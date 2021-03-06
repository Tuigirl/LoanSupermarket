package jingcheng.LoanSupermarket.user.dao;

import jingcheng.LoanSupermarket.user.entity.AppStartUpPage;
import jingcheng.LoanSupermarket.user.entity.AppVersion;
import jingcheng.LoanSupermarket.user.entity.Feedback;
import jingcheng.LoanSupermarket.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 用户模块数据层接口
 */
@Repository("userDao")
public interface UserDao {

    /**
     * 查看启动页信息
     */
    AppStartUpPage appStartUpPage();

    /**
     * 查看app最新版本信息
     */
    AppVersion appVersion(int versionType);

    /**
     * 查看手机号是否存在
     */
    int userPhone(String userPhone);

    /**
     * 根据手机号查询验证码
     */
    String userCode(String userPhone);

    /**
     * 将验证码存储到数据库
     */
    void insertUserCode(Map<String,Object> map);

    /**
     * 修改验证码
     */
    void updateUserCode(Map<String,Object> map);

    /**
     * 删除验证码信息
     */
    void deleteUserCode(String userPhone);

    /**
     * 注册保存信息
     */
    void register(User user);

    /**
     * 修改密码
     */
    void passWord(Map<String,Object> map);

    /**
     * 登陆验证
     */
    User login(Map<String,Object> map);

    /**
     * 添加意见反馈
     */
    void feedback(Feedback feedback);

    /**
     * 修改最后登陆时间
     */
    void loginLastTime(Long userId);

}
