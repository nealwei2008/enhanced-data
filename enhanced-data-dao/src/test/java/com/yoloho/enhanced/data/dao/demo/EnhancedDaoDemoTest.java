package com.yoloho.enhanced.data.dao.demo;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yoloho.enhanced.data.dao.api.EnhancedDao;
import com.yoloho.enhanced.data.dao.api.UpdateEntry;
import com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter;
import com.yoloho.enhanced.data.dao.impl.EnhancedDaoImpl;
import com.yoloho.enhanced.data.dao.impl.EnhancedDaoImplTest.UnitTestUser;
import com.yoloho.enhanced.data.dao.impl.EnhancedDaoImplTest.UnitTestUserMapping;

/**
 * EnhancedDao demo
 * Test with different packages scanning 
 * @author jason
 */
public abstract class EnhancedDaoDemoTest {
    private final static Logger logger = LoggerFactory.getLogger(EnhancedDaoDemoTest.class.getSimpleName());
    
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource(name = "unitTestUserEnhancedDao")
    EnhancedDao<UnitTestUser, Integer> dao;
    @Resource(name = "unitTestUserMappingEnhancedDao")
    EnhancedDaoImpl<UnitTestUserMapping, Integer> unitTestUserMappingEnhancedDao;
    
    /**
     * 简单主键
     */
    @Test
    public void getFindTest() {
        EnhancedDaoImpl<UnitTestUser, Integer> dao = new EnhancedDaoImpl<UnitTestUser, Integer>();
        dao.setSqlSessionFactory(sqlSessionFactory);
        dao.setTableName(UnitTestUser.class);
        /**
         * remove and insert
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.equalPair("name", "myname");
            dao.remove(filter.getQueryData());
        }
        {
            for (int i = 1; i < 10; i++) {
                UnitTestUser user = new UnitTestUser();
                user.setId(i);
                user.setName("myname");
                UnitTestUser insertAndReturn = dao.insertAndReturn(user);
                assertEquals("myname", insertAndReturn.getName());
            }
        }
        /**
         * get
         */
        {
            Assertions.assertNull(dao.get(0));
        }
        {
            UnitTestUser user = dao.get(1);
            Assertions.assertEquals(1, user.getId());
            Assertions.assertEquals("test", user.getIgnoreKye());
        }
        {
            UnitTestUser bean = dao.get("id", 1);
            Assertions.assertNotNull(bean);
            Assertions.assertTrue(bean.getId() == 1);
        }
        {
            List<UnitTestUser> list = dao.find(1, 2, 3, 4, 5, 6, 16);
            Assertions.assertNotNull(list);
            Assertions.assertTrue(list.size() > 1);
            Assertions.assertTrue(list.get(0).getId() > 0);
        }
        {
            List<UnitTestUser> list = dao.find("id", 1, 100);
            Assertions.assertNotNull(list);
            Assertions.assertTrue(list.size() == 1);
            Assertions.assertTrue(list.get(0).getId() > 0);
        }
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.equalPair("id", 1);
            UnitTestUser user = dao.get(filter.getQueryData());
            Assertions.assertEquals(1, user.getId());
            Assertions.assertEquals("test", user.getIgnoreKye());
        }
        /**
         * find
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            List<UnitTestUser> list = dao.find(filter.getQueryData());
            Assertions.assertNotNull(list);
            Assertions.assertTrue(list.size() > 1);
            Assertions.assertTrue(list.get(0).getId() > 0);
        }
        /**
         * count
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            int count = dao.count(filter.getQueryData());
            Assertions.assertTrue(count > 0);
        }
        /**
         * sum
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            int sum = dao.sum("id", filter.getQueryData());
            Assertions.assertTrue(sum > 0);
        }
        /**
         * remove
         */
        {
            dao.remove(new DynamicQueryFilter().equalPair("name", "myname").getQueryData());
        }
    }
    /**
     * 读删写测试
     */
    @Test
    public void insertRemoveTest() {
        unitTestUserMappingEnhancedDao.setBatchSize(3);
        List<Integer> idList = Lists.newArrayList();
        /**
         * 不带自增
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            logger.info("remove {} records", unitTestUserMappingEnhancedDao.remove(filter.getQueryData()));
        }
        {
            UnitTestUserMapping bean = new UnitTestUserMapping();
            bean.setUid(1);
            bean.setOtherId(101);
            bean.setMemo("user 1 contains ` special chars");
            bean.setDateline((int)(System.currentTimeMillis() / 1000));
            Assertions.assertEquals(1, unitTestUserMappingEnhancedDao.insert(bean));
            idList.add(1);
        }
        {
            List<UnitTestUserMapping> list = Lists.newArrayList();
            for (int i = 2; i < 19; i++) {
                UnitTestUserMapping bean = new UnitTestUserMapping();
                bean.setUid(i);
                bean.setOtherId(100 + i);
                bean.setMemo(String.format("user %d contains `&*^%%(^&^*%%@'\" special chars", i));
                bean.setDateline((int)(System.currentTimeMillis() / 1000) + i);
                list.add(bean);
                idList.add(i);
            }
            Assertions.assertEquals(17, unitTestUserMappingEnhancedDao.insert(list));
        }
        {
            //update
            UnitTestUserMapping mapping = unitTestUserMappingEnhancedDao.get(10);
            Assertions.assertNotNull(mapping);
            Assertions.assertEquals(10, mapping.getUid());
            mapping.setMemo("new val");
            Assertions.assertEquals(1, unitTestUserMappingEnhancedDao.update(mapping));
            mapping = unitTestUserMappingEnhancedDao.get(10);
            Assertions.assertNotNull(mapping);
            Assertions.assertEquals(10, mapping.getUid());
            Assertions.assertEquals("new val", mapping.getMemo());
        }
        {
            //update batch
            String tail = " update append new val";
            List<UnitTestUserMapping> mappingList = unitTestUserMappingEnhancedDao.find(4, 5, 6);
            Assertions.assertNotNull(mappingList);
            Assertions.assertEquals(3, mappingList.size());
            for (UnitTestUserMapping unitTestUserMapping : mappingList) {
                unitTestUserMapping.setMemo(unitTestUserMapping.getMemo() + tail);
            }
            Assertions.assertEquals(3, unitTestUserMappingEnhancedDao.update(mappingList));
            mappingList = unitTestUserMappingEnhancedDao.find(4, 5, 6);
            Assertions.assertNotNull(mappingList);
            Assertions.assertEquals(3, mappingList.size());
            Assertions.assertTrue(mappingList.get(0).getMemo().contains(tail));
        }
        {
            // 试验性功能，条件批量修改
            String newVal = "batch update!!!!%";
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.greaterOrEqual("otherId", 113);
            filter.lessThan("otherId", 117);
            Map<String, UpdateEntry> data = Maps.newHashMap();
            UpdateEntry entryMemo = new UpdateEntry();
            entryMemo.setValue(newVal);
            data.put("memo", entryMemo);
            UpdateEntry entryDateline = new UpdateEntry();
            entryDateline.setValue("@dateline@ - @dateline@ - 100000000");
            entryDateline.setPlain(true);
            data.put("dateline", entryDateline);
            UpdateEntry otherIdEntry = new UpdateEntry();
            otherIdEntry.setPlain(true);
            otherIdEntry.setValue("@otherId@ + 100");
            data.put("otherId", otherIdEntry);
            Assertions.assertEquals(4, unitTestUserMappingEnhancedDao.update(data, filter.getQueryData()));
            //check old
            List<UnitTestUserMapping> list = unitTestUserMappingEnhancedDao.find(filter.getQueryData());
            Assertions.assertEquals(0, list.size());
            //check new
            filter = new DynamicQueryFilter();
            filter.greaterOrEqual("otherId", 213);
            filter.lessThan("otherId", 217);
            list = unitTestUserMappingEnhancedDao.find(filter.getQueryData());
            Assertions.assertEquals(4, list.size());
            for (UnitTestUserMapping unitTestUserMapping : list) {
                Assertions.assertEquals(-100000000, unitTestUserMapping.getDateline());
                Assertions.assertEquals(entryMemo.getValue(), unitTestUserMapping.getMemo());
            }
        }
        {
            //clean data
            DynamicQueryFilter filter = new DynamicQueryFilter();
            filter.in("uid", idList);
            Assertions.assertEquals(idList.size(), unitTestUserMappingEnhancedDao.remove(filter.getQueryData()));
            logger.info("remove {} records", idList.size());
        }
    }
}
