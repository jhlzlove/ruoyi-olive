<template>
  <div class="panel-tab__content">
    <div class="panel-tab__content--title">
      <span><i class="el-icon-menu" style="margin-right: 8px; color: #555555"></i>消息列表</span>
      <el-button size="small" class="custom-button-size" type="primary" icon="Plus" @click="openModel('message')">创建新消息</el-button>
    </div>
    <el-table :data="messageList" size="small" border>
      <el-table-column type="index" label="序号" width="60px" />
      <el-table-column label="消息ID" prop="id" max-width="300px" show-overflow-tooltip />
      <el-table-column label="名称" prop="name" max-width="300px" show-overflow-tooltip />
      <el-table-column label="操作" min-width="100px">
        <template #default="{ row, $index }">
          <el-button size="small" link type="primary" @click="updateMessage(row, $index)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button size="small" link type="danger" @click="removeMessage(row, $index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="panel-tab__content--title" style="padding-top: 8px; margin-top: 8px; border-top: 1px solid #eeeeee">
      <span><i class="el-icon-menu" style="margin-right: 8px; color: #555555"></i>信号列表</span>
      <el-button size="small" class="custom-button-size" type="primary" icon="Plus" @click="openModel('signal')">创建新信号</el-button>
    </div>
    <el-table :data="signalList" size="small" border>
      <el-table-column type="index" label="序号" width="60px" />
      <el-table-column label="信号ID" prop="id" max-width="300px" show-overflow-tooltip />
      <el-table-column label="名称" prop="name" max-width="300px" show-overflow-tooltip />
      <el-table-column label="范围" prop="scope" max-width="100px" show-overflow-tooltip />
      <el-table-column label="操作" min-width="100px" fixed="right">
        <template #default="{ row, $index }">
          <el-button size="small" link type="primary" @click="updateSignal(row, $index)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button size="small" link type="danger" @click="removeSignal(row, $index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="panel-tab__content--title" style="padding-top: 8px; margin-top: 8px; border-top: 1px solid #eeeeee">
      <span><i class="el-icon-menu" style="margin-right: 8px; color: #555555"></i>错误列表</span>
      <el-button size="small" class="custom-button-size" type="primary" icon="Plus" @click="openModel('error')">创建新错误</el-button>
    </div>
    <el-table :data="errorList" size="small" border>
      <el-table-column type="index" label="序号" width="60px" />
      <el-table-column label="错误ID" prop="id" max-width="300px" show-overflow-tooltip />
      <el-table-column label="名称" prop="name" max-width="300px" show-overflow-tooltip />
      <el-table-column label="标识" prop="errorCode" max-width="300px" show-overflow-tooltip />
      <el-table-column label="操作" min-width="100px" fixed="right">
        <template #default="{ row, $index }">
          <el-button size="small" link type="primary" @click="updateError(row, $index)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button size="small" link type="danger" @click="removeError(row, $index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="modelVisible" :title="modelConfig.title" :close-on-click-modal="false" width="400px" append-to-body destroy-on-close>
      <el-form :model="modelObjectForm" label-width="90px" @submit.native.prevent>
        <el-form-item :label="modelConfig.idLabel">
          <el-input v-model="modelObjectForm.id" clearable >
            <!--id为纯数字时,保存xml会校验不通过-->
            <template #prepend v-if="modelType === 'signal'">Signal_</template>
            <template #prepend v-else-if="modelType === 'message'">Message_</template>
            <template #prepend v-else>Error_</template>
          </el-input>
        </el-form-item>
        <el-form-item :label="modelConfig.nameLabel">
          <el-input v-model="modelObjectForm.name" clearable />
        </el-form-item>
        <el-form-item :label="modelConfig.codeLabel" v-if="modelType === 'error'">
          <el-input v-model="modelObjectForm.errorCode" clearable />
        </el-form-item>
        <el-form-item label="全局信号" v-if="modelType === 'signal'">
          <el-switch v-model="modelObjectForm.scope" active-value="global" inactive-value="processInstance"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button size="small" class="custom-button-size" @click="modelVisible = false">取 消</el-button>
          <el-button type="primary" size="small" class="custom-button-size" @click="addNewObject">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SignalMessageErrorPanel">
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();
const rootElements = ref([]);
const messageIdMap = ref({});
const signalIdMap = ref({});
const errorIdMap = ref({});
const signalList = ref([]);
const messageList = ref([]);
const errorList = ref([]);
const modelVisible = ref(false);
const modelType = ref("");

const data = reactive({
  modelObjectForm: {
    // 默认当前实例有效
    scope: 'processInstance'
  },
  elementTypeMap: {
    message: "bpmn:Message",
    signal: "bpmn:Signal",
    error: "bpmn:Error",
  },
  prefixMap: {
    message: "Message_",
    signal: "Signal_",
    error: "Error_",
  },
});

const {modelObjectForm, elementTypeMap, prefixMap} = toRefs(data);

const modelConfig = computed(() => {
  if (modelType.value === "message") {
    return { title: "消息", idLabel: "消息ID", nameLabel: "消息名称" };
  } else if (modelType.value === "signal") {
    return { title: "信号", idLabel: "信号ID", nameLabel: "信号名称" };
  } else {
    return { title: "错误", idLabel: "错误ID", nameLabel: "错误名称", codeLabel: "错误标识" };
  }
})


onMounted(() => {
  initDataList();
})

function initDataList() {
  rootElements.value = modelerStore.getModeler.getDefinitions().rootElements;
  messageIdMap.value = {};
  signalIdMap.value = {};
  messageList.value = [];
  signalList.value = [];
  rootElements.value.forEach(el => {
    if (el.$type === "bpmn:Message") {
      messageIdMap.value[el.id] = true;
      messageList.value.push({...el});
    }
    if (el.$type === "bpmn:Signal") {
      signalIdMap.value[el.id] = true;
      signalList.value.push({...el});
    }
    if (el.$type === "bpmn:Error") {
      errorIdMap.value[el.id] = true;
      errorList.value.push({...el});
    }
  });
}

function openModel(type) {
  modelType.value = type;
  modelObjectForm.value = {};
  modelVisible.value = true;
}

function addNewObject() {
  const elementType = elementTypeMap.value[modelType.value];
  const prefix = prefixMap.value[modelType.value];
  // 检查 id 是否已经以 prefix 开头
  if (modelObjectForm.value.id.indexOf(prefix) !== 0) {
    modelObjectForm.value.id = prefix + modelObjectForm.value.id;
  }
  const elementIndex = rootElements.value.findIndex(element => element.$type === elementType && element.id === modelObjectForm.value.id);
  if (elementIndex > -1) {
    // 更新元素
    rootElements.value[elementIndex] = modelerStore.getModdle.create(elementType, modelObjectForm.value);
  } else {
    // 新增元素
    const elementRef = modelerStore.getModdle.create(elementType, modelObjectForm.value);
    rootElements.value.push(elementRef);
  }
  modelVisible.value = false;
  initDataList();
}

// 编辑消息-对id也进行修改时,会新增新增操作
function updateMessage(attr, index) {
  modelType.value = "message";
  const prefix = "Message_";
  modelVisible.value = true;
  const {id, name} = attr;
  const messageId = id.startsWith(prefix) ? id.substring(prefix.length) : id;
  modelObjectForm.value = index === -1 ? {} : {id: messageId, name};
}

// 移除消息
function removeMessage(attr, index) {
  proxy.$confirm("确认移除该消息吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    messageList.value.splice(index, 1);
    removeRootElements('bpmn:Message', attr.id);
  }).catch(() => console.info("操作取消"))
}

// 编辑信号-对id也进行修改时,会新增新增操作
function updateSignal(attr, index) {
  modelType.value = "signal";
  const prefix = "Signal_";
  modelVisible.value = true;
  const {id, name, scope} = attr;
  const signalId = id.startsWith(prefix) ? id.substring(prefix.length) : id;
  modelObjectForm.value = index === -1 ? {} : {id: signalId, name, scope};
}

// 移除信号
function removeSignal(attr, index) {
  proxy.$confirm("确认移除该信号吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    signalList.value.splice(index, 1);
    removeRootElements('bpmn:Signal', attr.id);
  }).catch(() => console.info("操作取消"));
}


// 编辑错误-对id也进行修改时,会新增新增操作
function updateError(attr, index) {
  modelType.value = "error";
  const prefix = "Error_";
  modelVisible.value = true;
  const {id, name, scope} = attr;
  const signalId = id.startsWith(prefix) ? id.substring(prefix.length) : id;
  modelObjectForm.value = index === -1 ? {} : {id: signalId, name, scope};
}

// 移除错误
function removeError(attr, index) {
  proxy.$confirm("确认移除该错误吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    errorList.value.splice(index, 1);
    removeRootElements('bpmn:Error', attr.id);
  }).catch(() => console.info("操作取消"));
}

// 删除主流程中的元素
function removeRootElements(elementType, elementId) {
  rootElements.value?.forEach((ex, index) => {
    if (ex.$type === elementType && ex.id === elementId) {
      rootElements.value.splice(index, 1);
    }
  });
}
</script>
