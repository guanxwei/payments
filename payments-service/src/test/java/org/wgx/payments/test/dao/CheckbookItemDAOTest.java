package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.model.CheckbookItem;
import org.wgx.payments.tools.ObjectGenerator;
import org.wgx.payments.utils.DateUtils;

public class CheckbookItemDAOTest extends DAOTestBase {

    @Resource
    private CheckbookItemDAO checkbookItemDAO;

    @Test
    public void testSave() throws Exception {
        CheckbookItem item = ObjectGenerator.generate(CheckbookItem.class);
        int result = checkbookItemDAO.save(item);
        assertEquals(result, 1);
    }

    @Test
    public void testGetListByTransactionID() throws Exception {
        CheckbookItem item = ObjectGenerator.generate(CheckbookItem.class);
        checkbookItemDAO.save(item);
        List<CheckbookItem> list = checkbookItemDAO.getListByTransactionID(item.getTransactionID());
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getAcknowledgedAmount(), item.getAcknowledgedAmount());
    }

    @Test
    public void testList() throws Exception {
        CheckbookItem item = ObjectGenerator.generate(CheckbookItem.class);
        checkbookItemDAO.save(item);
        List<CheckbookItem> list = checkbookItemDAO.list(20, 0, item.getStatus());
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(list.size() - 1).getAcknowledgedAmount(), item.getAcknowledgedAmount());
    }

    @Test
    public void testFindByUniqueItem() throws Exception {
        CheckbookItem item = ObjectGenerator.generate(CheckbookItem.class);
        checkbookItemDAO.save(item);
        CheckbookItem result = checkbookItemDAO.findByUniqueItem(item.getUniqueKey());
        assertNotNull(result);
        assertEquals(result.getAcknowledgedAmount(), item.getAcknowledgedAmount());
    }

    @Test
    public void testGetCheckbookItemsByRange() {
        Timestamp begin = new Timestamp(System.currentTimeMillis() - 100000);
        Timestamp end = new Timestamp(System.currentTimeMillis() + 100000);
        List<CheckbookItem> list = checkbookItemDAO.getCheckbookItemsByRange(DateUtils.convertFromTimestamp(begin), DateUtils.convertFromTimestamp(end));
        assertTrue(list.size() > 0);
    }
}
