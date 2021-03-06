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
package net.demo.hasor.core;
import net.demo.hasor.core.db.DataSourceModule;
import net.demo.hasor.core.filters.JumpFilter;
import net.demo.hasor.core.filters.VarFilter;
import net.demo.hasor.core.servlet.MyServlet;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
/**
 * 
 * @version : 2015年12月25日
 * @author 赵永春(zyc@hasor.net)
 */
public class MyModule extends WebModule {
    @Override
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        //
        apiBinder.filter("/*").through(new JumpFilter(apiBinder.getEnvironment()));
        apiBinder.filter("/*").through(new VarFilter(apiBinder.getEnvironment()));
        //
        apiBinder.serve("/myServlet.do").with(MyServlet.class);
        //
        apiBinder.installModule(new DataSourceModule());
    }
}
