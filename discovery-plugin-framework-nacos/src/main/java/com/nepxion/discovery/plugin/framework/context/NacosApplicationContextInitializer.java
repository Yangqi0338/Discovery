package com.nepxion.discovery.plugin.framework.context;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.ApplicationInfoAdapter;
import com.nepxion.discovery.plugin.framework.constant.NacosConstant;
import com.nepxion.discovery.plugin.framework.decorator.NacosServiceRegistryDecorator;
import com.nepxion.discovery.plugin.framework.util.MetadataUtil;

public class NacosApplicationContextInitializer extends PluginApplicationContextInitializer {
    @Override
    protected Object afterInitialization(ConfigurableApplicationContext applicationContext, Object bean, String beanName) throws BeansException {
        if (bean instanceof NacosServiceRegistry) {
            NacosServiceRegistry nacosServiceRegistry = (NacosServiceRegistry) bean;

            NacosDiscoveryProperties nacosDiscoveryProperties = applicationContext.getBean(NacosDiscoveryProperties.class);

            return new NacosServiceRegistryDecorator(nacosDiscoveryProperties, nacosServiceRegistry, applicationContext);
        } else if (bean instanceof NacosDiscoveryProperties) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            NacosDiscoveryProperties nacosDiscoveryProperties = (NacosDiscoveryProperties) bean;

            Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();

            String groupKey = PluginContextAware.getGroupKey(environment);
            if (!metadata.containsKey(groupKey)) {
                metadata.put(groupKey, DiscoveryConstant.DEFAULT);
            }
            if (!metadata.containsKey(DiscoveryConstant.VERSION)) {
                metadata.put(DiscoveryConstant.VERSION, DiscoveryConstant.DEFAULT);
            }
            if (!metadata.containsKey(DiscoveryConstant.REGION)) {
                metadata.put(DiscoveryConstant.REGION, DiscoveryConstant.DEFAULT);
            }
            String prefixGroup = getPrefixGroup(applicationContext);
            if (StringUtils.isNotEmpty(prefixGroup)) {
                metadata.put(groupKey, prefixGroup);
            }
            String gitVersion = getGitVersion(applicationContext);
            if (StringUtils.isNotEmpty(gitVersion)) {
                metadata.put(DiscoveryConstant.VERSION, gitVersion);
            }

            metadata.put(DiscoveryConstant.SPRING_BOOT_VERSION, SpringBootVersion.getVersion());
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_NAME, PluginContextAware.getApplicationName(environment));
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_TYPE, PluginContextAware.getApplicationType(environment));
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_PLUGIN, NacosConstant.NACOS_TYPE);
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_VERSION, DiscoveryConstant.DISCOVERY_VERSION);
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_REGISTER_CONTROL_ENABLED, PluginContextAware.isRegisterControlEnabled(environment).toString());
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED, PluginContextAware.isDiscoveryControlEnabled(environment).toString());
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_CONFIG_REST_CONTROL_ENABLED, PluginContextAware.isConfigRestControlEnabled(environment).toString());
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_GROUP_KEY, groupKey);
            metadata.put(DiscoveryConstant.SPRING_APPLICATION_CONTEXT_PATH, PluginContextAware.getContextPath(environment));

            try {
                ApplicationInfoAdapter applicationInfoAdapter = applicationContext.getBean(ApplicationInfoAdapter.class);
                if (applicationInfoAdapter != null) {
                    metadata.put(DiscoveryConstant.APP_ID, applicationInfoAdapter.getAppId());
                }
            } catch (Exception e) {

            }

            MetadataUtil.filter(metadata);

            return bean;
        } else {
            return bean;
        }
    }
}