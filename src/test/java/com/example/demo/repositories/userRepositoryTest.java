package com.example.demo.repositories;

import com.example.demo.data.model.Privilege;
import com.example.demo.data.model.Role;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.RoleRepository;
import com.example.demo.data.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class userRepositoryTest {

    @Autowired
    private UserRepository user;

    @Autowired
    private RoleRepository roles;

    @Test
    public void findByNameTest() {
        Role doctorRole = new Role();
        roles.save(doctorRole);
        User user1 = new User("HasanTest","Hasan",doctorRole);
        user.save(user1);
        Assert.assertNotNull(user.findByUserName("HasanTest"));
    }


}