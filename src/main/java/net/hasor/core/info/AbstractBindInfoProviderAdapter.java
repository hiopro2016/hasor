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
package net.hasor.core.info;
import org.more.util.StringUtils;
import net.hasor.core.BindInfo;
import net.hasor.core.BindInfoBuilder;
import net.hasor.core.Provider;
import net.hasor.core.Scope;
/**
 * 用于定义Bean，实现了Bean配置接口{@link BindInfoBuilder}，配置的信息通过{@link BindInfo}接口展现出来。
 * <p>同时实现了{@link CustomerProvider}和{@link ScopeProvider}接口。表示着这个Bean定义支持自定义{@link Provider}和{@link Scope}。
 * @version : 2014年7月3日
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class AbstractBindInfoProviderAdapter<T> extends MetaDataAdapter implements BindInfoBuilder<T>, BindInfo<T>, CustomerProvider<T>, ScopeProvider {
    //1.基本属性
    private String             bindID           = null;
    private String             bindName         = null;
    private Class<T>           bindType         = null;
    private Class<? extends T> sourceType       = null;
    private Boolean            singleton        = null;
    //2.系统属性
    private Provider<T>        customerProvider = null;
    private Provider<Scope>    scopeProvider    = null;
    //
    public String getBindID() {
        if (this.bindID == null) {
            this.bindID = this.bindType.getName() + "#" + this.bindName;
        }
        return this.bindID;
    }
    public void setBindID(String newID) {
        if (StringUtils.isBlank(newID) == true) {
            throw new NullPointerException("newID is null.");
        }
        this.bindID = newID;
    }
    public void setBindName(final String bindName) {
        this.bindName = bindName;
    }
    public String getBindName() {
        return this.bindName;
    }
    public Class<T> getBindType() {
        return this.bindType;
    }
    public void setBindType(final Class<T> bindType) {
        this.bindType = bindType;
    }
    public void setSourceType(final Class<? extends T> sourceType) {
        this.sourceType = sourceType;
    }
    public Class<? extends T> getSourceType() {
        return this.sourceType;
    }
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }
    public Boolean isSingleton() {
        return this.singleton;
    }
    public void setCustomerProvider(final Provider<T> customerProvider) {
        this.customerProvider = customerProvider;
    }
    /**获取 {@link #setCustomerProvider(Provider)} 方法设置的 Provider 对象。*/
    public Provider<T> getCustomerProvider() {
        return this.customerProvider;
    }
    public void setScopeProvider(final Provider<Scope> scopeProvider) {
        this.scopeProvider = scopeProvider;
    }
    public Provider<Scope> getScopeProvider() {
        return this.scopeProvider;
    }
    public BindInfo<T> toInfo() {
        return this;
    }
}