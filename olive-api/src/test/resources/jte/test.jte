@param com.olive.base.util.Model model
package ${model.packageName()}

@Service
public class ${model.serviceName()}Service {
    private final JSqlClient sqlClient;

    public ${model.serviceName()}Service(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }
}