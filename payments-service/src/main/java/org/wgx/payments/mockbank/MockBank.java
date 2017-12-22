package org.wgx.payments.mockbank;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wgx.payments.deducer.Deducer;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock bank to mock the bank behaviors. When write integration test cases, you may need to update here
 * to mock different kinds of payment responses.
 *
 */
@Controller
@RequestMapping(path = "/payments/mockbank")
@Slf4j
public class MockBank {

    @Resource
    private Map<Role, RoleAction> actions;

    @Resource
    private Deducer<HttpServletRequest, Role> roleDeducer;

    /**
     * Mock bank entrance.
     * @param request Http request.
     * @param bankname Bank name.
     * @param operation Operation type.
     * @return Mocked result.
     */
    @RequestMapping(path = "/{bankname}/{operation}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String mockResponse(final HttpServletRequest request, @PathVariable(value = "bankname") final String bankname,
            @PathVariable(value = "operation") final String operation) {
        log.info("Incoming request for bank: {}, operation: {}", bankname, operation);
        Role role = findRule(request);
        if (role == null) {
            return "Please set up roles before integrating with mock bank";
        }
        RoleAction action = actions.get(role);
        return action.act(request);
    }

    private Role findRule(final HttpServletRequest request) {
        Role role = roleDeducer.deduce(request);
        if (role == null) {
            log.warn("Can not find any role matching the incoming request. Request: [{}]", request);
            return null;
        } else {
            log.info("Find role: {} to execute on the incoming request", role.name());
            return role;
        }
    }

}
