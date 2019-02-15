package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.model.CheckOrderDiffItem;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.ObjectGenerator;

public class CheckOrderDiffDAOTest extends DAOTestBase {

    @Resource
    private CheckOrderDiffDAO checkOrderDiffDAO;

    @Test
    public void testsave() throws Exception {
        CheckOrderDiffItem item = ObjectGenerator.generate(CheckOrderDiffItem.class);
        assertEquals(checkOrderDiffDAO.save(item), 1);
    }

    @Test
    public void testfind() throws Exception {
        CheckOrderDiffItem item = ObjectGenerator.generate(CheckOrderDiffItem.class);
        checkOrderDiffDAO.save(item);
        CheckOrderDiffItem list = checkOrderDiffDAO.find(item.getId());
        assertNotNull(item);
        assertEquals(Jackson.json(item), Jackson.json(list));
    }

    @Test
    public void testlist() throws Exception {
        CheckOrderDiffItem item = ObjectGenerator.generate(CheckOrderDiffItem.class);
        checkOrderDiffDAO.save(item);
        List<CheckOrderDiffItem> list = checkOrderDiffDAO.list(20, 0, item.getStatus());
        assertNotNull(item);
        assertTrue(list.size() > 0);
        list.parallelStream().forEach(checkItem -> {
            assertEquals(item.getStatus(), checkItem.getStatus());
        });
    }

    @Test
    public void testupdateStatus() throws Exception {
        CheckOrderDiffItem item = ObjectGenerator.generate(CheckOrderDiffItem.class);
        checkOrderDiffDAO.save(item);
        assertEquals(checkOrderDiffDAO.updateStatus(item.getId(), 12345678), 1);
        CheckOrderDiffItem list = checkOrderDiffDAO.find(item.getId());
        assertNotNull(item);
        assertEquals(list.getStatus(), 12345678);
    }

    @Test
    public void testcount() throws Exception {
        CheckOrderDiffItem item = ObjectGenerator.generate(CheckOrderDiffItem.class);
        checkOrderDiffDAO.save(item);
        assertEquals(checkOrderDiffDAO.count(item.getStatus()), 1);
    }
}
