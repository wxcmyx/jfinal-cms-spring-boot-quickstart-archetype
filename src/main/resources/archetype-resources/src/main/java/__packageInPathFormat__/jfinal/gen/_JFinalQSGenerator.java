package ${package}.jfinal.gen;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.generator.Generator;

import javax.sql.DataSource;

/**
 * 在数据库表有任何变动时，运行一下 main 方法，极速响应变化进行代码重构
 */
public class _JFinalQSGenerator {

    private static DataSource getMasterDataSource() {
        Prop prop = PropKit.use("application-dev.properties");
        String url = prop.get("spring.datasource.ds1.url");
        String username = prop.get("spring.datasource.ds1.username");
        String password = prop.get("spring.datasource.ds1.password");
        HikariCpPlugin druidPlugin = new HikariCpPlugin(url, username, password);
        druidPlugin.start();
        return druidPlugin.getDataSource();
    }

    private static void configExcludingTable(Generator generator) {
        generator.addExcludedTable("adv");
    }

    public static void main(String[] args) {
        // base model 所使用的包名
        String baseModelPackageName = "${package}.jfinal.model.base";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/${package}/jfinal/model/base";

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "${package}.jfinal.model";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        // 创建生成器
        DataSource dataSource = getMasterDataSource();
        MyGenerator generator = new MyGenerator(dataSource, baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        // 添加不需要生成的表名
        configExcludingTable(generator);
        // 设置是否在 Model 中生成 dao 对象
        generator.setGenerateDaoInModel(true);
        // 设置是否生成字典文件
        generator.setGenerateDataDictionary(true);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
        generator.setRemovedTableNamePrefixes("sys_");
        generator.addIncludedTable("githubproject");
        // 生成
        generator.generate();
    }
}
