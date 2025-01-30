package com.amarhu.service;

import com.amarhu.production.entity.Production;
import com.amarhu.production.service.ProductionLastMonthService;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class ProductionLastMonthServiceTest {

    @Autowired
    private ProductionLastMonthService productionLastMonthService;

    @Autowired
    private UserRepository userRepository;


}
