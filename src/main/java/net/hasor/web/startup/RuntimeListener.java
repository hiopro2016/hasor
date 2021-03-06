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
package net.hasor.web.startup;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.more.util.ExceptionUtils;
import org.more.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.hasor.core.AppContext;
import net.hasor.core.Module;
import net.hasor.web.WebAppContext;
import net.hasor.web.binder.ListenerPipeline;
import net.hasor.web.context.WebTemplateAppContext;
/**
 * 
 * @version : 2013-3-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class RuntimeListener implements ServletContextListener, HttpSessionListener {
    protected Logger           logger                  = LoggerFactory.getLogger(getClass());
    public static final String AppContextName          = AppContext.class.getName();
    private WebAppContext      appContext              = null;
    private ListenerPipeline   sessionListenerPipeline = null;
    /*----------------------------------------------------------------------------------------------------*/
    //
    /**创建{@link WebAppContext}对象*/
    protected WebAppContext createAppContext(final ServletContext sc) throws Throwable {
        return WebTemplateAppContext.create("hasor-config.xml", sc);
    }
    //
    /**获取启动模块*/
    protected Module getStartModule(ServletContext sc) throws Exception {
        //
        //1.Start Module.
        Module startModule = null;
        String startModuleType = sc.getInitParameter("startModule");
        if (StringUtils.isBlank(startModuleType)) {
            logger.info("web initModule is undefinition.");
        } else {
            Class<Module> startModuleClass = (Class<Module>) Thread.currentThread().getContextClassLoader().loadClass(startModuleType);
            startModule = startModuleClass.newInstance();
            logger.info("web initModule is " + startModuleType);
        }
        return startModule;
    }
    //
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            //1.create AppContext
            final ServletContext sc = servletContextEvent.getServletContext();
            this.appContext = this.createAppContext(sc);
            if (this.appContext.isStart() == false) {
                Module startModule = this.getStartModule(sc);
                this.appContext.start(startModule);
            }
            //
        } catch (Throwable e) {
            throw ExceptionUtils.toRuntimeException(e);
        }
        //2.获取SessionListenerPipeline
        this.sessionListenerPipeline = this.appContext.getInstance(ListenerPipeline.class);
        this.sessionListenerPipeline.init(this.appContext);
        logger.info("sessionListenerPipeline created.");
        //3.放入ServletContext环境。
        logger.info("ServletContext Attribut is " + RuntimeListener.AppContextName);
        servletContextEvent.getServletContext().setAttribute(RuntimeListener.AppContextName, this.appContext);
        this.sessionListenerPipeline.contextInitialized(servletContextEvent);
    }
    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        if (this.sessionListenerPipeline != null) {
            this.sessionListenerPipeline.contextDestroyed(servletContextEvent);
        }
        this.appContext.shutdown();
        logger.info("shutdown.");
    }
    @Override
    public void sessionCreated(final HttpSessionEvent se) {
        if (this.sessionListenerPipeline != null) {
            this.sessionListenerPipeline.sessionCreated(se);
        }
    }
    @Override
    public void sessionDestroyed(final HttpSessionEvent se) {
        if (this.sessionListenerPipeline != null) {
            this.sessionListenerPipeline.sessionDestroyed(se);
        }
    }
    //
    /**获取{@link AppContext}*/
    public static AppContext getAppContext(ServletContext servletContext) {
        return (AppContext) servletContext.getAttribute(RuntimeListener.AppContextName);
    }
}