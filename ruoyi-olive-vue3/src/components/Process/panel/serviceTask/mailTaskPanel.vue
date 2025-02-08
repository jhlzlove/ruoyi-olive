<template>
  <div style="margin-top: 16px">
    <el-descriptions class="margin-top" :column="1" border>
      <template #extra>
        <el-button type="primary" @click="handleChangeMailTask">详情/编辑</el-button>
      </template>
      <el-descriptions-item label="接收者" label-class-name="mail-detail">{{mailTaskForm.to}}</el-descriptions-item>
      <el-descriptions-item label="发送者" label-class-name="mail-detail">{{mailTaskForm.from}}</el-descriptions-item>
      <el-descriptions-item label="主题" label-class-name="mail-detail">{{mailTaskForm.subject}}</el-descriptions-item>
      <el-descriptions-item label="抄送人" label-class-name="mail-detail">{{mailTaskForm.cc}}</el-descriptions-item>
      <el-descriptions-item label="暗送人" label-class-name="mail-detail">{{mailTaskForm.bcc}}</el-descriptions-item>
    </el-descriptions>
    <el-dialog title="邮件配置" v-model="mailModelVisible" width="60%" append-to-body destroy-on-close>
      <el-form :model="mailTaskForm" label-width="100px" ref="taskFormRef" class="mail-task-form">
        <el-form-item label="接收者" prop="to" :rules="{ required: true, trigger: ['blur', 'change'] }">
          <el-input v-model="mailTaskForm.to" placeholder="邮件的接受者,可以使用逗号分隔多个接受者" clearable class="input-with-select">
            <template #prepend>
              <el-select v-model="mailTaskForm.toType">
                <el-option v-for="i in Object.keys(typeObject)" :key="i" :label="typeObject[i]" :value="i" />
              </el-select>
            </template>
          </el-input>
        </el-form-item>
          <el-form-item label="发送者" prop="from">
            <el-input v-model="mailTaskForm.from" placeholder="邮件发送者的地址,如果不提供,会使用默认配置的地址" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="mailTaskForm.toType">
                  <el-option v-for="i in Object.keys(typeObject)" :key="i" :label="typeObject[i]" :value="i" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="主题" prop="subject">
            <el-input v-model="mailTaskForm.subject" placeholder="邮件的主题" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="mailTaskForm.toType">
                  <el-option v-for="i in Object.keys(typeObject)" :key="i" :label="typeObject[i]" :value="i" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="抄送人" prop="cc">
            <el-input v-model="mailTaskForm.cc" placeholder="邮件抄送人,可以使用逗号分隔多个接收者" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="mailTaskForm.toType">
                  <el-option v-for="i in Object.keys(typeObject)" :key="i" :label="typeObject[i]" :value="i" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="暗送人" prop="bcc">
            <el-input v-model="mailTaskForm.bcc" placeholder="邮件暗送人,可以使用逗号分隔多个接收者" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="mailTaskForm.toType">
                  <el-option v-for="i in Object.keys(typeObject)" :key="i" :label="typeObject[i]" :value="i" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮件类型" prop="html">
            <el-radio-group v-model="mailTaskForm.mailType" class="ml-4">
              <el-radio label="text">纯文本</el-radio>
              <el-radio label="html">HTML</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="邮件内容" prop="text" v-if="mailTaskForm.mailType === 'text'">
            <el-input v-model="mailTaskForm.text" type="textarea" :rows="2" placeholder="邮件内容(纯文本)" clearable />
          </el-form-item>
          <el-form-item label="邮件内容" prop="html" v-else>
            <editor v-model="mailTaskForm.html" :min-height="192"/>
          </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="mailModelVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveMailTask">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MailTaskPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const {proxy} = getCurrentInstance();

const mailModelVisible = ref(false);

const data = reactive({
  mailTaskForm: {
    to: '',
    toType: 'string',
    from: '',
    fromType: 'string',
    subject: '',
    subjectType: 'string',
    cc: '',
    ccType: 'string',
    bcc: '',
    bccType: 'string',
    mailType: 'text',
    html: '',
    text: '',
  },
  mailTaskObject: {
    to: '',
    from: '',
    subject: '',
    cc: '',
    bcc: '',
    html: '',
    text: '',
  },
  typeObject: {
    string: "字符串",
    expression: "表达式"
  },
});

const {mailTaskForm, mailTaskObject, typeObject} = toRefs(data);

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
    fieldsList.forEach(field => {
      mailTaskForm.value[field.name] = field.string || field.expression;
      mailTaskForm.value[field.name + `Type`] = field.string ? "string" : "expression";
    })
    mailTaskForm.value.mailType = mailTaskForm.value.text ? "text" : "html";
  }
}

function handleChangeMailTask() {
  mailModelVisible.value = true;
}

// 创建扩展属性
function saveMailTask() {
  proxy.$refs["taskFormRef"].validate((valid) => {
    if (valid) {
      const fieldsList = [];
      for (let key in mailTaskObject.value) {
        const value = mailTaskForm.value[key];
        if (value) {
          const fieldType = mailTaskForm.value[key + `Type`] || "string";
          const fieldConfig = fieldType === "string" ? {name: key, string: value} : {
            name: key,
            expression: value
          };
          const mailFrom = modelerStore.getModdle.create(`flowable:Field`, fieldConfig);
          fieldsList.push(mailFrom);
        }
      }

      updateElementExtensions(fieldsList);
      mailModelVisible.value = false;
    }
  });
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

<style>
.mail-task-form .el-select .el-input {
  width: 100px;
}
.input-with-select .el-input-group__prepend {
  background-color: #fff;
}
.mail-detail {
  width: 80px;
}
</style>
