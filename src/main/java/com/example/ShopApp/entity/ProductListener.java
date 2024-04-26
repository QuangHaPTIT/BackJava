package com.example.ShopApp.entity;

import com.example.ShopApp.sevices.impl.ProductRedisServiceImpl;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


@AllArgsConstructor
@NoArgsConstructor
public class ProductListener {
    @Autowired
    private ProductRedisServiceImpl productRedisService;

    private static final Logger logger = LoggerFactory.getLogger(ProductListener.class);


    @PrePersist
    public void prePersist(Product product) {
        logger.info("prePersist");
    }

    @PostPersist
    public void postPersist(Product product) {
        logger.info("postPersist");
    }

    @PreUpdate
    public void preUpdate(Product product) {
        //ApplicationEventPublisher.instance().publishEvent(event);
        logger.info("preUpdate");
    }

    @PostUpdate
    public void postUpdate(Product product) {
        // Update Redis cache
        logger.info("postUpdate");
        productRedisService.clear();
    }

    @PreRemove
    public void preRemove(Product product) {
        logger.info("preRemove");
    }

    @PostRemove
    public void postRemove(Product product) {
        //Update Redis cache
        logger.info("postRemove");
        productRedisService.clear();
    }
}
