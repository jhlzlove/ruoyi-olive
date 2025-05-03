@param com.olive.generator.model.GeneratorModel model
package ${model.packageName()}

@Service
public class ${model.modelName()}Service {
    private final JSqlClient sqlClient;

    public ${model.modelName()}Service(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }
}