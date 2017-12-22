package org.wgx.payments.mockbank.wechat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;

/**
 * Mock bank action to return checkbook.
 *
 */
public class WechatCheckbookAction implements RoleAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
       try (InputStream input = this.getClass().getResourceAsStream("/mockbank/wechat.checkbook");
               BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));) {
           String content = reader.readLine();
           StringBuilder sb = new StringBuilder();
           while (content != null) {
               sb.append(content).append("\n");
               content = reader.readLine();
           }
           return sb.toString();
       } catch (Exception e) {
           return "Error";
       }
    }

}
