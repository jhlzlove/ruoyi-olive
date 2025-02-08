<template>
  <div style="margin-top: 16px">
    <el-form label-width="100px">
      <el-form-item label="脚本格式">
        <el-input v-model="scriptTaskForm.scriptFormat" clearable @input="updateElementTask()" @change="updateElementTask()" />
      </el-form-item>
      <el-form-item label="脚本类型">
        <el-select v-model="scriptTaskForm.scriptType">
          <el-option label="内联脚本" value="inline" />
          <el-option label="外部资源" value="external" />
        </el-select>
      </el-form-item>
      <el-form-item label="脚本" v-show="scriptTaskForm.scriptType === 'inline'">
        <el-input
          v-model="scriptTaskForm.script"
          type="textarea"
          resize="vertical"
          :autosize="{ minRows: 2, maxRows: 4 }"
          clearable
          @input="updateElementTask()"
          @change="updateElementTask()"
        />
      </el-form-item>
      <el-form-item label="资源地址" v-show="scriptTaskForm.scriptType === 'external'">
        <el-input v-model="scriptTaskForm.resource" clearable @input="updateElementTask()" @change="updateElementTask()" />
      </el-form-item>
      <el-form-item>
        <template #label>
            <span>
               结果变量
               <el-tooltip placement="top">
                   <template #content>
                     <div>
                        服务执行的返回值（仅对使用表达式的服务任务），可以通过为服务任务定义的'flowable:resultVariable'属性设置为流程变量。
                       <br />可以是已经存在的，或者新的流程变量。 如果指定为已存在的流程变量，则流程变量的值会被服务执行的结果值覆盖。
                       <br />如果不指定结果变量名，则服务任务的结果值将被忽略。
                     </div>
                  </template>
                 <el-icon><QuestionFilled /></el-icon>
               </el-tooltip>
            </span>
        </template>
        <el-input v-model="scriptTaskForm.resultVariable" clearable @input="updateElementTask()" @change="updateElementTask()" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="ScriptTaskPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const data = reactive({
  defaultTaskForm: {
    scriptFormat: "",
    script: "",
    resource: "",
    resultVariable: ""
  },
  scriptTaskForm: {}
});

const { defaultTaskForm, scriptTaskForm} = toRefs(data);


/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        proxy.$nextTick(() => resetTaskForm());
      }
    },
    {immediate: true}
)

function resetTaskForm() {
  for (let key in defaultTaskForm.value) {
    let value = modelerStore.getBpmnElement?.businessObject[key] || defaultTaskForm.value[key];
    Reflect.set(scriptTaskForm.value, key, value);
  }
  Reflect.set(scriptTaskForm.value, "scriptType", scriptTaskForm.value.script ? "inline" : "external");
  deleteElementFormExtensions();
}

function updateElementTask() {
  let taskAttr = Object.create(null);
  taskAttr.scriptFormat = scriptTaskForm.value.scriptFormat || null;
  taskAttr.resultVariable = scriptTaskForm.value.resultVariable || null;
  if (scriptTaskForm.value.scriptType === "inline") {
    taskAttr.script = scriptTaskForm.value.script || null;
    taskAttr.resource = null;
  } else {
    taskAttr.resource = scriptTaskForm.value.resource || null;
    taskAttr.script = null;
  }
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 新建节点时,默认是用户节点,会添加表单相关扩展属性,当前任务直接删除掉
function deleteElementFormExtensions() {
  const element = modelerStore.getBpmnElement;
  if (element.businessObject?.extensionElements?.values) {
    element.businessObject.extensionElements.values =
        element.businessObject.extensionElements.values.filter(ex => ex.$type !== 'flowable:FormItems');
  }
}
</script>
