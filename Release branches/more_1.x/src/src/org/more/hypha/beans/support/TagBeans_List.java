/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.more.hypha.beans.support;
import java.util.ArrayList;
import org.more.hypha.beans.define.List_ValueMetaData;
import org.more.hypha.configuration.DefineResourceImpl;
/**
 * ���ڽ���list��ǩ
 * @version 2010-9-16
 * @author ������ (zyc@byshell.org)
 */
public class TagBeans_List extends TagBeans_AbstractCollection<List_ValueMetaData> {
    /**����{@link TagBeans_List}����*/
    public TagBeans_List(DefineResourceImpl configuration) {
        super(configuration);
    }
    /**����{@link List_ValueMetaData}����*/
    protected List_ValueMetaData createDefine() {
        return new List_ValueMetaData();
    }
    protected Class<?> getDefaultCollectionType() {
        return ArrayList.class;
    }
}