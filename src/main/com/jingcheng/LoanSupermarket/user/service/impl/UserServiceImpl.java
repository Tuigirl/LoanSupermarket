package jingcheng.LoanSupermarket.user.service.impl;

import jingcheng.LoanSupermarket.user.dao.UserDao;
import jingcheng.LoanSupermarket.user.entity.AppStartUpPage;
import jingcheng.LoanSupermarket.user.entity.AppVersion;
import jingcheng.LoanSupermarket.user.entity.Feedback;
import jingcheng.LoanSupermarket.user.entity.User;
import jingcheng.LoanSupermarket.user.service.UserService;
import jingcheng.utils.MD5.MD5Util;
import jingcheng.utils.message.MessageUtil;
import jingcheng.utils.response.ErrorMessage;
import jingcheng.utils.response.ReqResponse;
import jingcheng.utils.token.TokenUtil;
import org.apache.http.client.ClientProtocolException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块逻辑层实现
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    /**
     * 查看最近的启动页
     */
    @Override
    public ReqResponse startupPage() {
        ReqResponse req = new ReqResponse();
        try{
            AppStartUpPage page = userDao.appStartUpPage();
            req.setResult(page);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("数据加载完成");
        }catch(Exception e){
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("数据加载失败");
        }
        return req;
    }

    /**
     * 查看app版本号
     */
    @Override
    public ReqResponse appVersion(String device_type) {
        ReqResponse req = new ReqResponse();
        try{
            AppVersion version = userDao.appVersion(Integer.valueOf(device_type));
            req.setResult(version);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("数据加载完成");
        }catch(Exception e){
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("数据加载失败");
        }
        return req;
    }

    /**
     * 发送短信验证码
     * type 1注册(查看手机号是否占用) 2找回密码(查看手机号是否存在)
     */
    @Override
    public ReqResponse sendMessage(String userPhone, int type) throws ClientProtocolException, IOException {
        ReqResponse req = new ReqResponse();
        if(type == 1 || type == 2){
            if(type == 1){
                int num = userDao.userPhone(userPhone);
                if(num > 0){
                    req.setCode(ErrorMessage.FAIL.getCode());
                    req.setMessage("手机号已经被占用");
                    return req;
                }
            //2找回密码(查看手机号是否存在)
            }else{
                int num = userDao.userPhone(userPhone);
                if(num == 0){
                    req.setCode(ErrorMessage.FAIL.getCode());
                    req.setMessage("账号信息不存在");
                    return req;
                }
            }
            String code = MessageUtil.code();
            Map<String,Object> map = new HashMap<>();
            map.put("userPhone",userPhone);
            map.put("code",code);
            //根据手机号查询验证码
            String userCode =userDao.userCode(userPhone);
            if(null == userCode){
                //将验证码存储到数据库
                userDao.insertUserCode(map);
            }else{
                //修改验证码
                userDao.updateUserCode(map);
            }
            MessageUtil.sendMessage(userPhone,code);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("短信发送成功");
        }else{
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("参数错误");
        }
        return req;
    }

    /**
     * 验证短信验证码
     */
    @Override
    public ReqResponse verifyMessage(String userPhone, String messageCode) {
        ReqResponse req = new ReqResponse();
        String userCode = userDao.userCode(userPhone);
        if(messageCode.equals(userCode)){
            //验证成功,删除手验证码信息
            userDao.deleteUserCode(userPhone);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("验证成功");
        }else{
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("验证码错误");
        }
        return req;
    }

    /**
     * 账号注册
     */
    @Override
    public ReqResponse register(String device_type, String userPhone, String passWord) {
        ReqResponse req = new ReqResponse();
        //先验证一下手机号
        int num = userDao.userPhone(userPhone);
        if(num == 0){
            User user = new User();
            user.setUserPhone(userPhone);
            user.setPassWord(MD5Util.hexSALT(passWord,"user"));
            user.setUserUrl("http://dc.yqltt.cn/resources/user/system/user2x.png");
            user.setUserSex(0);
            user.setEmail(null);
            user.setCreateTime(new Date());
            user.setRegisterMethod(Integer.valueOf(device_type));
            user.setLastLoginTime(null);
            //注册保存信息
            userDao.register(user);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("注册成功");
        }else{
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("手机号被占用");
        }
        return req;
    }

    /**
     * 修改密码
     */
    @Override
    public ReqResponse passWord(String userPhone, String passWord) {
        ReqResponse req = new ReqResponse();
        //先验证一下手机号
        int num = userDao.userPhone(userPhone);
        if(num == 0){
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("账号信息不存在");
        }else{
            Map<String,Object> map = new HashMap<>();
            map.put("userPhone",userPhone);
            map.put("passWord",MD5Util.hexSALT(passWord,"user"));
            //修改密码
            userDao.passWord(map);
            req.setCode(ErrorMessage.SUCCESS.getCode());
            req.setMessage("修改成功");
        }
        return req;
    }

    /**
     * 账号登陆
     */
    @Override
    public ReqResponse login(String userPhone, String passWord) {
        ReqResponse req = new ReqResponse();
        Map<String,Object> map = new HashMap<>();
        map.put("userPhone",userPhone);
        map.put("passWord",MD5Util.hexSALT(passWord,"user"));
        User user = userDao.login(map);
        if(null == user){
            req.setCode(ErrorMessage.FAIL.getCode());
            req.setMessage("账号或密码错误");
        }else{
            if(user.getStatus() == 0){
                req.setCode(ErrorMessage.FAIL.getCode());
                req.setMessage("账号被禁用");
            }else{
                //修改最后登录时间
                userDao.loginLastTime(user.getId());
                Map<String,Object> userMap = new HashMap<>();
                userMap.put("userId",user.getId());
                userMap.put("userUrl",user.getUserUrl());
                req.setCode(ErrorMessage.SUCCESS.getCode());
                req.setMessage("登陆成功");
                req.setResult(userMap);
            }
        }
        return req;
    }

    /**
     * 添加意见反馈信息
     */
    @Override
    public ReqResponse feedback(Long userId, String userPhone, String content) {
        ReqResponse req = new ReqResponse();
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setCreateTime(new Date());
        feedback.setUserPhone(userPhone);
        userDao.feedback(feedback);
        req.setCode(ErrorMessage.SUCCESS.getCode());
        req.setMessage("反馈成功");
        return req;
    }


}
