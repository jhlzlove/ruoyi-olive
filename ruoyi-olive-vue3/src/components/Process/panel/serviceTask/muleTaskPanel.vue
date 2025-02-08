<template>
  <div style="margin-top: 16px">
      <el-form :model="muleTaskForm" label-width="100px">
        <el-form-item label="终端" prop="endpointUrl" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="muleTaskForm.endpointUrl" placeholder="需要调用的Mule终端" clearable @change="updateElementTask()"></el-input>
        </el-form-item>
          <el-form-item label="语言" prop="language" :rules="{ required: true, trigger: ['blur', 'change'] }">
            <el-input v-model="muleTaskForm.language" placeholder="要使用解析荷载表达式(payloadExpression)属性的语言" clearable @change="updateElementTask()"></el-input>
          </el-form-item>
          <el-form-item label="表达式" prop="payloadExpression" :rules="{ required: true, trigger: ['blur', 'change'] }">
            <el-input v-model="muleTaskForm.payloadExpression" placeholder="作为消息荷载的表达式" clearable @change="updateElementTask()"></el-input>
          </el-form-item>
          <el-form-item label="结果变量" prop="resultVariable">
            <el-input v-model="muleTaskForm.resultVariable" placeholder="将要保存调用结果的变量名称" clearable @change="updateElementTask()"></el-input>
          </el-form-item>
      </el-form>
  </div>
</template>

<script setup name="MuleTaskPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const mailModelVisible = ref(false);

const data = reactive({
  muleTaskForm: {
    endpointUrl: '',
    language: '',
    payloadExpression: '',
    resultVariable: '',
  },
});

const {muleTaskForm} = toRefs(data);

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
  // 获取注入字段
  const fieldsList = modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => ex.$type === `flowable:Field`) ?? [];
  if (fieldsList && fieldsList.length > 0) {
    fieldsList.forEach(field =>{
      muleTaskForm.value[field.name] = field.string;
    })
  }
}

// 创建扩展属性
function updateElementTask() {
  const fieldsList = [];
  for (let key in muleTaskForm.value) {
    const value = muleTaskForm.value[key];
    if (value) {
      const fieldConfig = {name: key, string: value};
      const mailFrom = modelerStore.getModdle.create(`flowable:Field`, fieldConfig);
      fieldsList.push(mailFrom);
    }
  }

  updateElementExtensions(fieldsList);
  mailModelVisible.value = false;
}

// 更新扩展属性
function updateElementExtensions(extensionList) {
  const extensions = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: extensionList
  });
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    extensionElements: extensions
  });
}
</script>
