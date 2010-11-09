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
package org.more.hypha.aop.support;
import org.more.hypha.Event;
import org.more.hypha.EventListener;
import org.more.hypha.aop.AopBeanDefinePlugin;
import org.more.hypha.aop.AopDefineResourcePlugin;
import org.more.hypha.beans.AbstractBeanDefine;
import org.more.hypha.configuration.DefineResourceImpl;
import org.more.hypha.event.EndBuildEvent;
import org.more.util.StringUtil;
/**
 * �����ǵ�{@link DefineResourceImpl}����{@link EndBuildEvent}�����¼�ʱ����anno:apply��ǩ���õ�Ӧ��Bean���������
 * @version 2010-10-11
 * @author ������ (zyc@byshell.org)
 */
public class TagAop_ToBeanApplyListener implements EventListener {
    private String config = null, toBeanExp = "*";
    //----------------------------------------------
    /**����{@link TagAop_ToBeanApplyListener}����*/
    public TagAop_ToBeanApplyListener(String config, String toBeanExp) {
        this.config = config;
        this.toBeanExp = toBeanExp;
    }
    /**ִ��BeanӦ�á�*/
    public void onEvent(Event event) {
        DefineResourceImpl config = (DefineResourceImpl) event.getTarget();
        AopDefineResourcePlugin aopPlugin = (AopDefineResourcePlugin) config.getPlugin(AopDefineResourcePlugin.AopDefineResourcePluginName);
        for (String defineName : config.getBeanDefineNames())
            if (StringUtil.matchWild(this.toBeanExp, defineName) == true) {
                AbstractBeanDefine define = config.getBeanDefine(defineName);
                if (define.getPlugin(AopBeanDefinePlugin.AopPluginName) == null)
                    aopPlugin.setAop(define, this.config);
            }
    }
}