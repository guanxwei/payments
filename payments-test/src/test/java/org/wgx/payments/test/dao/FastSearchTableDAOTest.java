package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.tools.ObjectGenerator;

public class FastSearchTableDAOTest extends DAOTestBase {

    @Resource
    private FastSearchTableDAO fastSearchTableDAO;

    @Test
    public void testsave() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        assertEquals(fastSearchTableDAO.save(item), 1);
    }

    @Test
    public void testfind() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        fastSearchTableDAO.save(item);
        FastSearchTableItem result = fastSearchTableDAO.find(item.getItemKey());
        assertEquals(item.getMessage(), result.getMessage());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getTime(), result.getTime());
        assertEquals(item.getStatus(), result.getStatus());
        assertEquals(item.getTransactionID(), result.getTransactionID());
    }

    @Test
    public void testlist() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        fastSearchTableDAO.save(item);
        List<FastSearchTableItem> list = fastSearchTableDAO.list(item.getItemKey(), item.getStatus());
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getId(), item.getId());
    }

    @Test
    public void testdeleteItem() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        fastSearchTableDAO.save(item);
        fastSearchTableDAO.deleteItem(item.getId());
        FastSearchTableItem result = fastSearchTableDAO.find(item.getItemKey());
        assertNull(result);
    }

    @Test
    public void testdeleteByKey() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        fastSearchTableDAO.save(item);
        fastSearchTableDAO.deleteItemByKey(item.getItemKey());
        FastSearchTableItem result = fastSearchTableDAO.find(item.getItemKey());
        assertNull(result);
    }

    @Test
    public void testfindItemByStatus() throws Exception {
        FastSearchTableItem item = ObjectGenerator.generate(FastSearchTableItem.class);
        fastSearchTableDAO.save(item);
        List<FastSearchTableItem> list = fastSearchTableDAO.findItemsByStatus(item.getStatus());
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getMessage(), item.getMessage());

    }
}
