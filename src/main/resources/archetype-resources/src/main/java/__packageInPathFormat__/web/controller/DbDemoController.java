package ${package}.web.controller;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import ${package}.jfinal.ext.ControllerUtils;
import ${package}.web.rest.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags="GIT项目管理")
@RestController
@EnableAutoConfiguration
public class DbDemoController {

    @Autowired
    private JdbcTemplate db;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

    @ApiOperation("获取项目总数")
    @GetMapping("/api/hello")
    @ResponseBody
    public Object home() {
        Map countMap=  db.queryForMap("select count(*) count from githubproject");
        Record record= Db.use("ds1").findFirst("select count(*) count from githubproject");
        record.set("other",countMap.get("count"));
        Record recordc=Db.use("ds1").findFirstByCache("User","count","select count(*) count from githubproject");
        record.set("recordc",recordc.getInt("count"));
        return new Result().setData(record.getColumns());
    }
    @ApiOperation("如何使用redis")
    @GetMapping("/api/redisuse")
    @ResponseBody
    public Object redisuse(){
        stringRedisTemplate.opsForValue().set("one",new Result().setCode(1).setData("oooooo").toString());
//        Redis.use().getSet("two",new Result().setCode(1).setData("222222").toString());
        return stringRedisTemplate.opsForValue().get("one");
    }
    @ApiOperation("如何使用jfinal分页")
    @GetMapping("/api/page")
    @ResponseBody
    public Object getGitHubpage(@ApiParam("查看第几页") @RequestParam int pageIndex,
                                @ApiParam("每页多少条") @RequestParam int pageSize) {
        Page<Record> gitpages= Db.use("ds1").paginate( pageIndex,pageSize,"select a.*,(select b.count from openopen b where a.update=b.date) count ","from githubproject a");
        return new Result().setData(ControllerUtils.recordsToCamelCaseMaps(gitpages));
    }
    @ApiOperation("如何使用mongodb")
    @GetMapping("/api/mongouse")
    @ResponseBody
    public Object mongouse(){
        mongoTemplate.save(new Result().setData("aaaa"),"test1");
        Query query=new Query(Criteria.where("code").is(0));
        Result result = mongoTemplate.findOne(query,Result.class,"test1");
        if(null == result){
            result=new Result().setCode(1).setErcode("11");
        }
        return result;
    }
}
