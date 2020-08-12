package org.wgx.payments.test.dao;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.dao.ActionRecordDAO;

@Configuration
public class DaoTestConfiguration {

    @Resource
    private ActionRecordDAO actionRecordDAO;

    @Bean
    public TestDaoService testDaoService() {
        TestDaoService service = new TestDaoService();
        service.setActionRecordDAO(actionRecordDAO);
        return service;
    }
}
