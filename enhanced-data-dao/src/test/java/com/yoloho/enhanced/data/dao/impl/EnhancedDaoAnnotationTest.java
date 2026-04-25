package com.yoloho.enhanced.data.dao.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yoloho.enhanced.common.util.Logging;
import com.yoloho.enhanced.data.dao.api.UpdateEntry;
import com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter;
import com.yoloho.enhanced.data.dao.impl.EnhancedDaoImplTest.UnitTestUserMapping;

public abstract class EnhancedDaoAnnotationTest {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDaoAnnotationTest.class.getSimpleName());
    
    @Resource
    private EnhancedDaoImpl<UnitTestUserMapping, Integer> unitTestUserMappingEnhancedDao;
    
    @BeforeEach
    public void init() {
        Logging.initLogging(true, false);
    }
    
    @Test
    public void insertRemoveTest() {
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
            //inc
            int oldDateline = mapping.getDateline();
            Map<String, UpdateEntry> data = Maps.newConcurrentMap();
            data.put("dateline", new UpdateEntry().increse(2));
            Assertions.assertEquals(1, unitTestUserMappingEnhancedDao.update(data, new DynamicQueryFilter()
                    .equalPair("uid", 10)
                    .getQueryData()));
            mapping = unitTestUserMappingEnhancedDao.get(10);
            Assertions.assertNotNull(mapping);
            Assertions.assertEquals(10, mapping.getUid());
            Assertions.assertEquals(oldDateline + 2, mapping.getDateline());
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
        /**
         * remove
         */
        {
            DynamicQueryFilter filter = new DynamicQueryFilter();
            logger.info("remove {} records", unitTestUserMappingEnhancedDao.remove(filter.getQueryData()));
        }
    }

}
