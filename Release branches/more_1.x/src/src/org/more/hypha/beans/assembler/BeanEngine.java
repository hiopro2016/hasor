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
package org.more.hypha.beans.assembler;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.more.ClassFormatException;
import org.more.DoesSupportException;
import org.more.RepeateException;
import org.more.hypha.AbstractBeanDefine;
import org.more.hypha.AbstractMethodDefine;
import org.more.hypha.AbstractPropertyDefine;
import org.more.hypha.ApplicationContext;
import org.more.hypha.ExpandPointManager;
import org.more.hypha.PropertyBinding;
import org.more.hypha.ValueMetaData;
import org.more.hypha.beans.define.PropertyDefine;
import org.more.hypha.expandpoint.AbstractExpandPointManager;
import org.more.hypha.expandpoint.point.AfterCreatePoint;
import org.more.hypha.expandpoint.point.BeforeCreatePoint;
import org.more.hypha.expandpoint.point.ClassBytePoint;
import org.more.hypha.expandpoint.point.ClassTypePoint;
import org.more.util.attribute.IAttribute;
/**
* �����ְ���Ǹ���{@link AbstractBeanDefine}ת�������ͻ���Beanʵ�����
* ������һ�������࣬��ʹ��ʱ��Ҫͨ�����������{@link AbstractExpandPointManager}����
* @version 2011-1-13
* @author ������ (zyc@byshell.org)
*/
public class BeanEngine {
    private Map<String, AbstractBeanBuilder<AbstractBeanDefine>> beanBuilderMap  = new HashMap<String, AbstractBeanBuilder<AbstractBeanDefine>>();
    private EngineClassLoader                                    classLoader     = null;
    private Map<String, Object>                                  singleBeanCache = new Hashtable<String, Object>();
    //----------------------------------------------------------------------------------------------------------
    class EngineClassLoader extends ClassLoader {
        public EngineClassLoader(ClassLoader parent) {
            super(parent);
        };
        public Class<?> loadClass(byte[] beanBytes, AbstractBeanDefine define) throws ClassFormatException {
            //���������Ҫװ�ص�����JVM�Ͳ�����ñ��ص�������ȥ���������Ƿ���ڡ�
            return this.defineClass(beanBytes, 0, beanBytes.length);
        };
    };
    public void init(ApplicationContext context, IAttribute flash) throws Throwable {
        super.init(context, flash);
        this.classLoader = new EngineClassLoader(this.getContext().getBeanClassLoader());//ʹ��EngineClassLoaderװ�ع�����ֽ���
    }
    //----------------------------------------------------------------------------------------------------------
    /**��ȡ��չ���������������Ҫ��д�÷������ṩ������չ���������*/
    protected ExpandPointManager getExpandPointManager() {
        return this.getContext().getExpandPointManager();
    };
    /**��յ�̬���еĵ���Bean��*/
    public void clearSingleBean() {
        this.singleBeanCache.clear();
    };
    /**
     * ע��һ��bean�������ͣ�ʹ֮���Ա��������������ظ�ע��ͬһ��bean���ͽ�������{@link RepeateException}�����쳣��
     * @param beanType ע���bean�������͡�
     * @param builder Ҫע���bean������������
     */
    public void regeditBeanBuilder(String beanType, AbstractBeanBuilder<AbstractBeanDefine> builder) throws RepeateException {
        if (this.beanBuilderMap.containsKey(beanType) == false)
            this.beanBuilderMap.put(beanType, builder);
        else
            throw new RepeateException("�����ظ�ע��[" + beanType + "]���͵�BeanBuilder��");
    };
    /**���ָ������bean�Ľ���֧�֣�����Ҫ�Ӵ�ע���bean�����Ƿ���ڸ÷������ᱻ��ȷִ�С�*/
    public void unRegeditBeanBuilder(String beanType) {
        if (this.beanBuilderMap.containsKey(beanType) == true)
            this.beanBuilderMap.remove(beanType);
    };
    /**������{@link BeanEngine}������������ĵ���Bean����*/
    public void clearBeanCache() {
        this.singleBeanCache.clear();
    };
    /**��ȡһ��int��int��ʾ��{@link BeanEngine}�������Ѿ������˵ĵ���������Ŀ��*/
    public int getCacheBeanCount() {
        return this.singleBeanCache.size();
    };
    //----------------------------------------------------------------------------------------------------------
    /**
     * ��{@link AbstractBeanDefine}��������������װ�س�ΪClass���Ͷ����ڼ��һ������{@link ClassBytePoint}��{@link ClassTypePoint}������չ�㡣
     * @param define Ҫװ�����͵�bean���塣
     * @param params 
     * @return ����װ�ص�bean���͡�
     * @throws DoesSupportException ���{@link BeanEngine}����һ����֧�ֵ�Bean������������������쳣��
     * @throws IOException �ڽ��ֽ���ת����Class���ʱ����������ⳣ����������
     * @throws ClassFormatException ��ִ����չ��{@link ClassTypePoint}������ܳɹ�װ���ֽ������������쳣��
     * @throws ClassNotFoundException �������װ�ع��̽���û�еõ�Class����������������쳣��
     */
    public final synchronized Class<?> builderType(AbstractBeanDefine define, Object[] params) throws DoesSupportException, IOException, ClassFormatException, ClassNotFoundException {
        String defineType = define.getBeanType();
        String defineID = define.getID();
        String defineLogStr = defineID + "[" + defineType + "]";//logǰ׺
        //1.
        AbstractBeanBuilder<AbstractBeanDefine> builder = this.beanBuilderMap.get(defineType);
        if (builder == null || builder.canBuilder() == false)
            throw new DoesSupportException(defineLogStr + "����Bean����һ��hypha��֧�ֵ�Bean���ͻ��߸����͵�Bean��֧��Builder��");
        //--------------------------------------------------------------------------------------------------------------׼���׶�
        //2.ͨ��Builderװ��class
        byte[] beanBytes = builder.loadBeanBytes(define);
        beanBytes = (byte[]) this.getExpandPointManager().exePointOnSequence(ClassBytePoint.class, //
                new Object[] { beanBytes, define, this.getContext() });//Param
        Class<?> beanType = null;
        beanType = (Class<?>) this.getExpandPointManager().exePointOnSequence(ClassTypePoint.class,//
                new Object[] { beanType, define, this.getContext() });//Param
        //3.
        if (beanType == null)
            beanType = this.classLoader.loadClass(beanBytes, define);
        if (beanType == null)
            throw new ClassNotFoundException(defineLogStr + "����Bean�����޷���װ�ػ�װ��ʧ�ܡ�");
        return beanType;
    };
    /**
     * �������洴������ȥ����bean���롣
     * @param define Ҫװ�����͵�bean���塣
     * @param params ����beanʱ���ݵĲ�����
     * @return ���ش�����bean����
     */
    public final synchronized Object builderBean(AbstractBeanDefine define, Object[] params) throws Throwable {
        String defineID = define.getID();
        //--------------------------------------------------------------------------------------------------------------��鵥̬
        //0.��̬
        if (this.singleBeanCache.containsKey(defineID) == true)
            return this.singleBeanCache.get(defineID);
        //--------------------------------------------------------------------------------------------------------------
        AbstractBeanBuilder<AbstractBeanDefine> builder = this.beanBuilderMap.get(define.getBeanType());
        Class<?> beanType = this.builderType(define);
        //1.����
        Object obj = this.getExpandPointManager().exePointOnReturn(BeforeCreatePoint.class, //Ԥ����Bean
                new Object[] { beanType, params, define, this.getContext() });
        if (obj == null)
            if (builder.ifDefaultBeanCreateMode() == false)
                //------------------------������Ĭ�ϲ��ԣ�
                obj = builder.createBean(define, params);
            else {
                //------------------------��ʹ��Ĭ�ϲ��ԣ�
                // create begin
                AbstractMethodDefine factory = define.factoryMethod();
                Collection<? extends AbstractPropertyDefine> initParam = null;
                if (factory != null) {
                    //1.������ʽ
                    initParam = factory.getParams();
                    Class<?>[] types = transform_toTypes(initParam, params);
                    Object[] objects = transform_toObjects(initParam, params);
                    String factoryBeanID = factory.getForBeanDefine().getID();
                    if (factory.isStatic() == true) {
                        Class<?> factoryType = this.getContext().getBeanType(factoryBeanID);
                        Method factoryMethod = factoryType.getMethod(factory.getCodeName(), types);
                        obj = factoryMethod.invoke(null, objects);
                    } else {
                        Object factoryObject = this.getContext().getBean(factoryBeanID, params);/*params�����ᱻ˳�ƴ��빤��bean�С�*/
                        Method factoryMethod = factoryObject.getClass().getMethod(factory.getCodeName(), types);
                        obj = factoryMethod.invoke(factoryObject, objects);
                    }
                } else {
                    //2.���췽��
                    initParam = define.getInitParams();
                    Class<?>[] types = transform_toTypes(initParam, params);
                    Object[] objects = transform_toObjects(initParam, params);
                    //
                    Constructor<?> constructor = beanType.getConstructor(types);
                    obj = constructor.newInstance(objects);
                }
                // create end//
            }
        //2.����ע��
        for (AbstractPropertyDefine propDefine : define.getPropertys()) {
            PropertyDefine prop = (PropertyDefine) propDefine;
            PropertyBinding eval = this.getContext().getELContext().getPropertyBinding(prop.getName(), obj);
            eval.setValue(this.transform_toObject(propDefine, params));//ELע��
        }
        //--------------------------------------------------------------------------------------------------------------��ʼ���׶�
        //3.��̬����&����ƥ��
        obj = this.cast(beanType, obj);
        if (define.isSingleton() == true)
            this.singleBeanCache.put(defineID, obj);
        //4.�������ٷ���
        {
            //TODO  ProxyFinalizeClassEngine ce = new ProxyFinalizeClassEngine(this);
            //TODO  ce.setBuilderMode(BuilderMode.Propxy);
            //TODO  ce.setSuperClass(objType);
            //TODO  obj = ce.newInstance(obj);
        }
        //5.ִ����չ��
        obj = this.getExpandPointManager().exePointOnSequence(AfterCreatePoint.class,//Beanװ��
                new Object[] { obj, params, define, this.getContext() });
        //6.ִ����������init����
        {
            Class<?> objType = obj.getClass();
            String initMethodName = define.getInitMethod();
            if (initMethodName != null) {
                Method m = objType.getMethod(initMethodName, Object[].class);
                m.invoke(obj, params);
            }
        }
        return obj;
    };
    /**�����������Ƿ�ƥ�䶨�����ͣ����û��ָ��beanType������ֱ�ӷ��ء�*/
    private Object cast(Class<?> beanType, Object obj) throws ClassCastException {
        if (beanType != null)
            return beanType.cast(obj);
        return obj;
    };
    /*��һ������ת�������͡�*/
    private Class<?>[] transform_toTypes(Collection<? extends AbstractPropertyDefine> pds, Object[] params) {
        System.out.println();
        return null;//TODO
    };
    /*��һ������ת���ɶ���*/
    private Object[] transform_toObjects(Collection<? extends AbstractPropertyDefine> pds, Object[] params) {
        System.out.println();
        return null;//TODO
    };
    /*������ת���ɶ���*/
    private Object transform_toObject(AbstractPropertyDefine prop, Object[] params) {
        System.out.println();
        return null;//TODO
    };
    private Collection<? extends ValueMetaData> transform(Collection<? extends AbstractPropertyDefine> pds) {
        ArrayList<ValueMetaData> vms = new ArrayList<ValueMetaData>(pds.size());
        for (AbstractPropertyDefine pd : pds)
            vms.add(pd.getMetaData());
        return vms;
    }
};
//class ProxyFinalizeClassEngine extends ClassEngine {
//    private final ProxyFinalizeClassBuilder builder = new ProxyFinalizeClassBuilder();
//    public ProxyFinalizeClassEngine(BeanEngine beanEngine) throws ClassNotFoundException {
//        super(false);
//    };
//    protected ClassBuilder createBuilder(BuilderMode builderMode) {
//        return this.builder;
//    };
//    public Object newInstance(Object propxyBean) throws FormatException, ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
//        Object obj = super.newInstance(propxyBean);
//        //this.toClass().getMethod("", parameterTypes);
//        return obj;
//    };
//};
//class ProxyFinalizeClassBuilder extends ClassBuilder {
//    protected ClassAdapter acceptClass(ClassWriter classVisitor) {
//        return new ClassAdapter(classVisitor) {
//            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                if (name.equals("finalize()V") == true)
//                    System.out.println();
//                return super.visitMethod(access, name, desc, signature, exceptions);
//            }
//        };
//    }
//};