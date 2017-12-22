package org.wgx.payments.mockbank;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.mockbank.alipay.AlipayCheckbookAction;
import org.wgx.payments.mockbank.alipay.AlipayCheckbookRole;
import org.wgx.payments.mockbank.alipay.ChargeAction;
import org.wgx.payments.mockbank.alipay.ChargeRole;
import org.wgx.payments.mockbank.alipay.RefundAction;
import org.wgx.payments.mockbank.alipay.RefundRole;
import org.wgx.payments.mockbank.alipay.RescindAction;
import org.wgx.payments.mockbank.alipay.RescindRole;
import org.wgx.payments.mockbank.alipay.ScheduledPayAction;
import org.wgx.payments.mockbank.alipay.ScheduledPayRole;
import org.wgx.payments.mockbank.alipay.SignAction;
import org.wgx.payments.mockbank.alipay.SignRole;
import org.wgx.payments.mockbank.alipay.VerifyAction;
import org.wgx.payments.mockbank.alipay.VerifyRole;
import org.wgx.payments.mockbank.callback.CallbackAction;
import org.wgx.payments.mockbank.callback.CallbackRole;
import org.wgx.payments.mockbank.wechat.WechatCheckbookAction;
import org.wgx.payments.mockbank.wechat.WechatCheckbookRole;

/**
 * Configuration class for mock bank.
 *
 */
@Configuration
public class MockBankConfig {

    /**
     * Role list definition method.
     * @return Role list.
     */
    @Bean(name = "roles")
    public List<Role> roles() {
        List<Role> roles = new LinkedList<>();
        roles.add(alipayChargeRole());
        roles.add(alipayVerifyRole());
        roles.add(alipayRefundRole());
        roles.add(alipaySignRole());
        roles.add(alipayRescindRole());
        roles.add(alipaySchedulePayRole());
        roles.add(wechatChargeRole());
        roles.add(wechatRefundRole());
        roles.add(wechatSignRole());
        roles.add(wechatScheduledPayRole());
        roles.add(wechatRescindRole());
        roles.add(callbackRole());
        roles.add(wechatCheckbookRole());
        roles.add(alipayCheckbookRole());
        return roles;
    }

    /**
     * Role and role action mapping directory.
     * @return Mapping directory.
     */
    @Bean(name = "actions")
    public Map<Role, RoleAction> actions() {
        Map<Role, RoleAction> actions = new HashMap<>();
        actions.put(alipayChargeRole(), alipayChargeAction());
        actions.put(alipayVerifyRole(), alipayVerifyAction());
        actions.put(alipayRefundRole(), alipayRefundAction());
        actions.put(alipaySignRole(), alipaySignAction());
        actions.put(alipayRescindRole(), alipayRescindAction());
        actions.put(alipaySchedulePayRole(), alipaySchedulePayAction());
        actions.put(wechatChargeRole(),  wechatChargeAction());
        actions.put(wechatRefundRole(), wechatRefundAction());
        actions.put(wechatSignRole(), wechatSignAction());
        actions.put(wechatScheduledPayRole(), wechatScheduledPayAction());
        actions.put(wechatRescindRole(), wechatRescindAction());
        actions.put(callbackRole(), callbackAction());
        actions.put(wechatCheckbookRole(), wechatCheckbookAction());
        actions.put(alipayCheckbookRole(), alipayCheckbookAction());
        return actions;
    }

    /**
     * Role deducer bean definition.
     * @return RoleDeducer.
     */
    @Bean(name = "roleDeducer")
    public Deducer<HttpServletRequest, Role> roleDeducer() {
        return new RoleDeducer();
    }

    /**
     * Alipay charge role config.
     * @return Alipay ChargeRole.
     */
    @Bean(name = "alipayChargeRole")
    public Role alipayChargeRole() {
        return new ChargeRole();
    }

    /**
     * Alipay charge role action config.
     * @return Alipay ChargeAction.
     */
    @Bean(name = "alipayChargeAction")
    public RoleAction alipayChargeAction() {
        return new ChargeAction();
    }

    /**
     * Alipay verify role config.
     * @return Alipay VerifyRole.
     */
    @Bean(name = "alipayVerifyRole")
    public Role alipayVerifyRole() {
        return new VerifyRole();
    }

    /**
     * Alipay verify role action config.
     * @return Alipay ChargVerifyActioneAction.
     */
    @Bean(name = "alipayVerifyAction")
    public RoleAction alipayVerifyAction() {
        return new VerifyAction();
    }

    /**
     * Alipay refund role config.
     * @return Alipay RefundRole.
     */
    @Bean(name = "alipayRefundRole")
    public Role alipayRefundRole() {
        return new RefundRole();
    }

    /**
     * Alipay refund role action config.
     * @return Alipay RefundAction.
     */
    @Bean(name = "alipayRefundAction")
    public RoleAction alipayRefundAction() {
        return new RefundAction();
    }

    /**
     * Alipay sign role config.
     * @return Alipay SignRole.
     */
    @Bean(name = "alipaySignRole")
    public Role alipaySignRole() {
        return new SignRole();
    }

    /**
     * Alipay sign role action config.
     * @return Alipay SignAction.
     */
    @Bean(name = "alipaySignAction")
    public RoleAction alipaySignAction() {
        return new SignAction();
    }

    /**
     * Alipay rescind role config.
     * @return Alipay RescindRole.
     */
    @Bean(name = "alipayRescindRole")
    public Role alipayRescindRole() {
        return new RescindRole();
    }

    /**
     * Alipay rescind role action config.
     * @return Alipay RescindAction.
     */
    @Bean(name = "alipayRescindAction")
    public RoleAction alipayRescindAction() {
        return new RescindAction();
    }

    /**
     * Alipay scheduled pay action config.
     * @return Alipay ScheduledPayAction
     */
    @Bean(name = "alipaySchedulePayAction")
    public RoleAction alipaySchedulePayAction() {
        return new ScheduledPayAction();
    }

    /**
     * Alipay scheduled pay role config.
     * @return ScheduledPayRole
     */
    @Bean(name = "alipaySchedulePayRole")
    public Role alipaySchedulePayRole() {
        return new ScheduledPayRole();
    }

    /**
     * Wechat charge role config.
     * @return Wechat ChargeRole.
     */
    @Bean(name = "wechatChargeRole")
    public Role wechatChargeRole() {
        return new org.wgx.payments.mockbank.wechat.ChargeRole();
    }

    /**
     * Wechat charge role action config.
     * @return Wechat ChargeAction.
     */
    @Bean(name = "wechatChargeAction")
    public RoleAction wechatChargeAction() {
        return new org.wgx.payments.mockbank.wechat.ChargeAction();
    }

    /**
     * Wechat refund role config.
     * @return Wechat ChargeRole.
     */
    @Bean(name = "wechatRefundRole")
    public Role wechatRefundRole() {
        return new org.wgx.payments.mockbank.wechat.RefundRole();
    }

    /**
     * Wechat refund role action config.
     * @return Wechat RefundAction.
     */
    @Bean(name = "wechatRefundAction")
    public RoleAction wechatRefundAction() {
        return new org.wgx.payments.mockbank.wechat.RefundAction();
    }

    /**
     * Wechat sign role config.
     * @return Wechat SignRole.
     */
    @Bean(name = "wechatSignRole")
    public Role wechatSignRole() {
        return new org.wgx.payments.mockbank.wechat.SignRole();
    }

    /**
     * Wechat sign role action config.
     * @return Wechat SignAction.
     */
    @Bean(name = "wechatSignAction")
    public RoleAction wechatSignAction() {
        return new org.wgx.payments.mockbank.wechat.SignAction();
    }

    /**
     * Wechat scheduled pay role config.
     * @return Wechat SignRole.
     */
    @Bean(name = "wechatScheduledPayRole")
    public Role wechatScheduledPayRole() {
        return new org.wgx.payments.mockbank.wechat.ScheduledPayRole();
    }

    /**
     * Wechat scheduled pay role action config.
     * @return Wechat ScheduledPayAction.
     */
    @Bean(name = "wechatScheduledPayAction")
    public RoleAction wechatScheduledPayAction() {
        return new org.wgx.payments.mockbank.wechat.ScheduledPayAction();
    }

    /**
     * Wechat rescind role config.
     * @return Wechat RescindRole.
     */
    @Bean(name = "wechatRescindRole")
    public Role wechatRescindRole() {
        return new org.wgx.payments.mockbank.wechat.RescindRole();
    }

    /**
     * Wechat rescind role action config.
     * @return Wechat RescindAction.
     */
    @Bean(name = "wechatRescindAction")
    public RoleAction wechatRescindAction() {
        return new org.wgx.payments.mockbank.wechat.RescindAction();
    }

    /**
     * CallbackRole configuration.
     * @return CallbackRole
     */
    @Bean
    public CallbackRole callbackRole() {
        return new CallbackRole();
    }

    /**
     * CallbackAction configuration.
     * @return CallbackAction
     */
    @Bean
    public CallbackAction callbackAction() {
        return new CallbackAction();
    }

    /**
     * AlipayCheckbookRole.
     * @return AlipayCheckbookRole
     */
    @Bean
    public AlipayCheckbookRole alipayCheckbookRole() {
        return new AlipayCheckbookRole();
    }

    /**
     * WechatCheckbookRole.
     * @return WechatCheckbookRole
     */
    @Bean
    public WechatCheckbookRole wechatCheckbookRole() {
        return new WechatCheckbookRole();
    }

    /**
     * AlipayCheckbookAction.
     * @return AlipayCheckbookAction
     */
    public AlipayCheckbookAction alipayCheckbookAction() {
        return new AlipayCheckbookAction();
    }

    /**
     * WechatCheckbookAction.
     * @return WechatCheckbookAction
     */
    public WechatCheckbookAction wechatCheckbookAction() {
        return new WechatCheckbookAction();
    }
}
