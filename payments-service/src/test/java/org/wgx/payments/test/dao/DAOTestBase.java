package org.wgx.payments.test.dao;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.wgx.payments.dao.DAOConfiguration;

@ContextConfiguration(classes = {
        DAOConfiguration.class,
        DaoTestConfiguration.class
    })
public abstract class DAOTestBase extends AbstractTestNGSpringContextTests {

}
