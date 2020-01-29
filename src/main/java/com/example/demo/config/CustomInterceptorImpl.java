package com.example.demo.config;

import java.io.Serializable;
import java.util.Arrays;

import org.hibernate.type.Type;
import org.hibernate.EmptyInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomInterceptorImpl extends EmptyInterceptor {
     
	private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(CustomInterceptorImpl.class);

    @Override
	public boolean onLoad(
			Object entity, 
			Serializable id, 
			Object[] state, 
			String[] propertyNames, 
			Type[] types) {
                logger.info("Entity:"+entity);
                logger.info("State[]:"+state);
                logger.info("PropertyNames[]:"+Arrays.toString(propertyNames));
                logger.info("Types[]:"+types); 
		return false;
	}

}
