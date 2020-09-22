package com.shop.service;

import com.dongyimai.pojo.TbSeller;
import com.dongyimai.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 */
public class UserDetailServiceImpl implements UserDetailsService {
    private SellerService sellerService;
    public void setSellerService(SellerService sellerService){
        this.sellerService=sellerService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = null;
        try {
            System.out.println("username:"+username);
            //System.out.println("sellerService: " + sellerService);

            //数据库查询
            TbSeller seller = sellerService.findOne(username);

            //System.out.println("seller : " + seller +"== stauts:"+seller.getStatus());
            if(seller!=null && "1".equals(seller.getStatus())){
                List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
                list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                return new User(username,seller.getPassword(),list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;

    }

}