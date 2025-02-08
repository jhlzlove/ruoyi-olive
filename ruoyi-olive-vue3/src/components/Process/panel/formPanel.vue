<template>
    <el-form label-width="80px" status-icon @submit.native.prevent>
      <el-form-item label="任务表单">
        <el-select v-model="bpmnFormData.formKey" clearable class="m-2" placeholder="配置任务表单" @change="updateElementFormKey" @clear="removeFormItem">
          <el-option
              v-for="item in formList"
              :key="item.formId"
              :label="item.formName"
              :value="item.formId">
            <span style="float: left">{{ item.formName }}</span>
            <span style="float: right">
                <dict-tag :options="form_type" :value="item.formType"/>
            </span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" class="custom-button-size" icon="View" @click="handleFormPermission">表单授权</el-button>
      </el-form-item>
    </el-form>

    <!-- 表单信息 -->
    <el-dialog title="表单信息" v-model="formOpen" width="65%" :destroy-on-close="true" append-to-body>
      <div class="top-right-status" >
        <el-row :gutter="10" class="mb8 mr10">
          <div>
            <span class="custom-badge-text">授权状态：</span>
            <span class="custom-badge success"></span><span class="custom-badge-text">可读写</span>
            <span class="custom-badge info"></span><span class="custom-badge-text">只读</span>
            <span class="custom-badge danger"></span><span class="custom-badge-text">不可见</span>
            <span class="custom-badge-stars"></span><span class="custom-badge-text">必填</span>
          </div>
        </el-row>
      </div>
      <el-table ref="dataTable" v-loading="loading" :row-key="getRowKey" :data="formWidgetList" max-height="600" border fit @selection-change="handleSelectionChange">
        <el-table-column type="selection" :reserve-selection="true" width="55" align="center" :selectable="selectable" />
        <el-table-column label="表单" prop="formName" min-width="50px">
          <template  #default="scope">
            <span>{{scope.row.formName}}</span>
            <span v-if="scope.row.nodeId === 'init'" class="ml5">(初始表单)</span>
          </template>
        </el-table-column>
        <el-table-column label="授权" prop="widgetType" min-width="50px">
          <template  #default="scope">
            <el-radio-group v-model="scope.row.widgetType" :disabled="firstUserTask && scope.row.nodeId === 'init'" @change="handleRadioChange(scope.row)">
              <el-radio label="readwrite">全读写</el-radio>
              <el-radio label="readonly">全只读</el-radio>
            </el-radio-group>
          </template>
        </el-table-column>
        <el-table-column label="字段授权(点击切换)" prop="formWidgets" min-width="200px">
          <template  #default="scope">
            <div :class="{ 'tag-disabled': firstUserTask && scope.row.nodeId === 'init' }">
              <el-tag style="margin-right: 5px;" v-for="(widget,index) in scope.row.formWidgets"
                      :key="index"
                      effect="plain"
                      class="clickable mx-1"
                      :type="getTagType(widget.type)"
                      @click="handleTagClick(scope.row.nodeId, widget)"
              >
                <!--invisible:不可见 readonly:只读 readwrite:可读写-->
                <el-tooltip class="item" :content="getTooltipContent(widget.type)" placement="top-start">
                  <div>
                    <el-icon>
                      <component :is="getIconClass(widget.type)"/>
                    </el-icon>
                    <span style="font-size: 13px">{{ widget.label }}</span><span class="custom-tag-stars" v-show="widget.required"></span>
                  </div>
                </el-tooltip>
            </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80px">
          <template  #default="scope">
            <el-button type="primary" link icon="View" size="small" @click="handleShowForm(scope.row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button size="small" class="custom-button-size" @click="formOpen = false">取 消</el-button>
          <el-button type="primary" size="small" class="custom-button-size" @click="saveFormPermission">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="60%" :destroy-on-close="true" append-to-body>
      <template #header>
        <span class="inner">{{viewForm.formName}}</span>
        <dict-tag class="inner" :options="form_type" :value="viewForm.formType"/>
      </template>
      <div class="test-form">
        <v-form-render v-if="renderForm" :form-data="formData" ref="vFormRef"/>
        <component v-else :is="formCompName" ref="vFormRef"></component>
      </div>
    </el-dialog>
</template>

<script setup name="formPanel">
import useModelerStore from '@/store/modules/modeler'
import {StrUtil} from "@/utils/StrUtil";
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();
const { form_type } = proxy.useDict('form_type');

// 是否是第一个用户任务节点
const firstUserTask = ref(false);
// 表单数据
const formList = ref([]);
const bpmnElement = ref(null);
// 是否渲染在线表单
const renderForm = ref(true);
const loading = ref(true);
// 路由表单组件名称
const formCompName = ref('');
// 表单渲染器
const vFormRef = ref(null);
// 表单查看
const formOpen = ref(false);
const viewOpen = ref(false);
// 表单组件属性列表
const formWidgetList = ref([]);
// 其它扩展属性列表
const otherExtensionList = ref([]);
// 扩展表单组件属性列表
const bpmnElementFormItemList = ref([]);
// 当前节点后面所有的用户任务节点
const userTasksAfterElement = ref([]);
// 选择表单授权列表
const multipleSelection = ref([]);


/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});

const data = reactive({
  bpmnFormData: {
    formKey: ""
  },
  formData:{},
  viewForm:{},
});

const { bpmnFormData, formData, viewForm } = toRefs(data);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        // 加载表单列表
        // getListForm();
        resetFlowForm();
      }
    },
    {immediate: true}
)
// 方法区
function resetFlowForm() {
  formWidgetList.value = [];
  bpmnElement.value = modelerStore.getBpmnElement;
  // 等待表单加载列表完成
  formList.value = modelerStore.getFormList;
  bpmnFormData.value.formKey = modelerStore.getBpmnElement.businessObject.formKey;
  otherExtensionList.value = []; // 其他扩展配置
  const bpmnElementProperties = bpmnElement.value.businessObject?.extensionElements?.values?.filter(ex => {
    if (ex.$type !== `flowable:FormItems`) {
      otherExtensionList.value.push(ex);
    }
    return ex.$type === `flowable:FormItems`;
  }) ?? [];
  // 保存当前节点所有的扩展表单权限信息
  bpmnElementFormItemList.value = getBpmnElementFormItemList(bpmnElementProperties);
  if (bpmnElementFormItemList.value.length > 0) {
    // 复制 显示
    copyAndDisplayFormWidgets();
  } else {
    // 初始化当前节点之前的所有用户任务节点
    initFormPermission();
    // 获取流程发起表单
    getInitForm();
  }
  // 递归查找当前节点之后的所有用户任务节点
  const currentElement = modelerStore.getElRegistry.get(props.id);
  userTasksAfterElement.value = [];
  findUserTasksAfterElement(currentElement, userTasksAfterElement.value);
  checkFirstUserTask(currentElement);
}
// 检验是否是第一个任务节点
function checkFirstUserTask(currentElement){
  const { businessObject } = currentElement;
  const process = businessObject.$parent;
  const firstTask = process.flowElements.find((element) => {
    return element.$type === 'bpmn:UserTask';
  });
  firstUserTask.value = !!(firstTask && businessObject.$type === 'bpmn:UserTask' && businessObject.id === firstTask.id);
}

// 获取当前节点所有的扩展表单权限信息
function getBpmnElementFormItemList(bpmnElementProperties) {
  return bpmnElementProperties.reduce((pre, current) => {
    if (current.values && current.values.length > 0) {
      return pre.concat(current.values);
    } else {
      return pre;
    }
  }, []);
}

// 复制显示表单权限
function copyAndDisplayFormWidgets() {
  // 处理当前节点已有扩展表单信息
  const widgetList = JSON.parse(JSON.stringify(bpmnElementFormItemList.value ?? []));
  formWidgetList.value = widgetList.map(item => {
    return {...item, formWidgets: JSON.parse(item.formWidgets)};
  });

  // 处理当前节点之前的表单信息,加载到当前节点上(前面节点可能删除或更换表单,当前节点也需要重新更换)
  const beforeTaskFormList = getBeforeTaskFormList() || [];
  beforeTaskFormList.forEach(element => {
    // 寻找表单列表中是否存在与当前元素相关的信息
    const index = formWidgetList.value.findIndex(item => item.formId.toString() === element.formKey && item.nodeId === element.nodeId);
    if (index === -1) {
      // 如果不存在相关信息，则从整个表单列表中筛选出匹配的表单，并为每个匹配的表单设置节点ID
      const filteredForms = formList.value.filter(form => element.formKey === form.formId.toString());
      filteredForms.forEach(form => {
        form.nodeId = element.nodeId;
        processWidgets(form);
      });
    }
  });
}

// 更新表单key
function updateElementFormKey(formId) {
  if (StrUtil.isNotBlank(formId)) {
    // 删除当前节点原有的表单扩展信息
    const formKey = modelerStore.getBpmnElement.businessObject.formKey;
    const index = findFormWidgetIndex(formKey);
    if (index !== -1){
      formWidgetList.value.splice(index, 1);
    }
    modelerStore.modeling.updateProperties(modelerStore.getBpmnElement, {'formKey': formId});
    const form = formList.value.find(item => item.formId === formId);
    if (form) {
      // 删除已配置的表单信息,更换节点表单后不进行替换,需要手动再次确认授权绑定
      const elIndex = findElementFormItemIndex(formKey);
      if (elIndex !== -1) {
        bpmnElementFormItemList.value.splice(elIndex, 1);
        if (bpmnElementFormItemList.value.length === 0) {
          deleteCurrentElementFormExtensions(modelerStore.getBpmnElement);
        } else {
          // 加入xml扩展属性中
          const tempForm = {
            ...form,
            nodeId: this.id,
            formWidgets: JSON.parse(form.formWidgets)
          };
          updateFormFields(tempForm);
        }
        // 变更或删除当前节点绑定的表单后,同时删除后续其他节点的引用,需要重新授权添加
        deleteAfterElementFormExtensions(formKey);
      }
      form.nodeId = props.id;
      processWidgets(form);
    }
  }
}

// 获取表单组件列表
function findFormWidgetIndex(formKey) {
  return formWidgetList.value.findIndex(item => item.formId.toString() === formKey && item.nodeId === props.id);
}

// 获取表单组件列表
function findElementFormItemIndex(formKey) {
  return bpmnElementFormItemList.value.findIndex(item => item.formId.toString() === formKey && item.nodeId === props.id);
}

// 获取表单组件列表
function findSelectionIndex(formKey) {
  return multipleSelection.value.findIndex(item => item.formId.toString() === formKey && item.nodeId === props.id);
}

// 表单选择变化
function handleSelectionChange(selection) {
  multipleSelection.value = JSON.parse(JSON.stringify(selection));
}

// 初始化当前节点表单权限
function initFormPermission() {
  const elementList = getCurrentAndBeforeTaskFormList() || [];
  if (formWidgetList.value.length === 0) {
    elementList.forEach(element => {
      const filteredForms = formList.value.filter(form => element.formKey === form.formId.toString());
      filteredForms.forEach(form => {
        form.nodeId = element.nodeId;
        processWidgets(form);
      });
    })
  }
}

// 获取流程发起表单
function getInitForm() {
  // 如果在表单组件列表中找不到nodeId为'init'的组件
  if (formWidgetList.value.findIndex(item => item.nodeId === 'init') === -1) {
    // 获取当前表单的formId
    const formId = modelerStore.getFormId;
    // 在表单列表中找到对应formId的表单
    const form = formList.value.find(item => item.formId === formId);
    // 如果表单存在
    if (form) {
      // 将表单的nodeId设置为'init'
      form.nodeId = 'init';
      // 处理表单组件
      processWidgets(form);
      // 加入xml扩展属性中
      const tempForm = {
        ...form,
        formWidgets: JSON.parse(form.formWidgets)
      };
      updateFormFields(tempForm);
    }
  }
}

// 获取当前节点之前节点表单信息
function getBeforeTaskFormList() {
  // 获取当前节点信息
  const currentElement = modelerStore.getElRegistry.get(props.id);
  const userTasksBeforeElement = [];
  findUserTasksBeforeElement(currentElement, userTasksBeforeElement);

  // 存储节点表单扩展信息?
  // bpmnElementFormItemList.value = extractFormItems(userTasksBeforeElement);

  // 获取当前节点之前节点表单信息
  return userTasksBeforeElement
      .filter(item => item.businessObject.formKey !== undefined)
      .map(item => {
        return {
          nodeId: item.id,
          formKey: item.businessObject.formKey
        };
      });
}

// 获取当前节点和之前节点的表单信息
function getCurrentAndBeforeTaskFormList() {
  // 获取当前节点信息
  const currentElement = modelerStore.getElRegistry.get(props.id);
  const userTasksBeforeElement = [];
  findUserTasksBeforeElement(currentElement, userTasksBeforeElement);
  userTasksBeforeElement.push(currentElement);

  // 提取用户任务节点的表单项信息
  bpmnElementFormItemList.value = extractFormItems(userTasksBeforeElement);

  // 获取当前节点之前节点表单信息
  return userTasksBeforeElement
      .filter(item => item.businessObject.formKey !== undefined)
      .map(item => {
        return {
          nodeId: item.id,
          formKey: item.businessObject.formKey
        };
      });
}

// 提取用户任务节点的表单项信息
function extractFormItems(userTasks) {
  const bpmnElementProperties = userTasks.flatMap(element => {
    const formItems = element.businessObject?.extensionElements?.values?.filter(ex => ex.$type === `flowable:FormItems`);
    if (formItems) {
      return formItems.flatMap(item => {
        if (item.nodeId !== 'init') {
          return item.values || [];
        }
        return [];
      });
    }
    return [];
  });

  return bpmnElementProperties.flatMap(item => item.values || []); // 返回扁平化处理后的表单项信息
}

// 递归查找当前节点之前的所有用户任务节点
function findUserTasksBeforeElement(currentNode, userTasks, visitedNodes = new Set()) {
  const incomingSequenceFlows = currentNode.incoming;
  incomingSequenceFlows.forEach(sequenceFlow => {
    const sourceElement = sequenceFlow.source;
    if (!visitedNodes.has(sourceElement)) {
      visitedNodes.add(sourceElement);
      if (sourceElement.type === 'bpmn:UserTask' && !userTasks.includes(sourceElement)) {
        userTasks.push(sourceElement);
      }
      findUserTasksBeforeElement(sourceElement, userTasks, visitedNodes);
    }
  });
}

// 递归查找当前节点之后的所有用户任务节点
function findUserTasksAfterElement(currentNode, userTasks, visitedNodes = new Set()) {
  const outgoingSequenceFlows = currentNode.outgoing;
  outgoingSequenceFlows.forEach(targetFlow => {
    const targetElement = targetFlow.target;
    if (!visitedNodes.has(targetElement)) {
      visitedNodes.add(targetElement);
      if (targetElement.type === 'bpmn:UserTask' && !userTasks.includes(targetElement)) {
        userTasks.push(targetElement);
      }
      findUserTasksAfterElement(targetElement, userTasks, visitedNodes);
    }
  });
}

// 查看表单权限
function handleFormPermission() {
  loading.value = true;
  if (formWidgetList.value.length === 0) {
    const form = formList.value.find(item => item.formId === bpmnFormData.value.formKey);
    if (form) {
      form.nodeId = props.id;
      processWidgets(form);
    }
  }
  formOpen.value = true;
  proxy.$nextTick(() => {
    if (proxy.$refs["dataTable"]) {
      // 将表单组件列表中的组件按照nodeId升序排序
      formWidgetList.value.sort((a, b) => {
        if (a.nodeId === 'init' && b.nodeId !== 'init') {
          return -1;
        } else if (a.nodeId !== 'init' && b.nodeId === 'init') {
          return 1;
        } else {
          return 0;
        }
      });
      proxy.$refs["dataTable"].clearSelection();
      formWidgetList.value.forEach(item => {
        proxy.$refs["dataTable"].toggleRowSelection(item, true);
      });
    }
  });
  loading.value = false;
}

// 初始化表单字段权限
function initElementExtensions() {
  formWidgetList.value.forEach(item => {
    initFormFields(item);
  });
}

// 保存表单字段权限
function saveFormPermission() {
  if (multipleSelection.value.length === 0) {
    proxy.$confirm("未勾选表单信息时,表单权限不会记录到节点!", "提示", {
      confirmButtonText: "确 认",
      cancelButtonText: "取 消",
      type: 'warning',
      center: true
    }).then(() => {
      deleteCurrentElementFormExtensions(modelerStore.getBpmnElement);
      formOpen.value = false;
    }).catch(() => console.info("操作取消"));
  } else {
    // 去除formItems中未勾选的表单信息,只保留已勾选的信息
    bpmnElementFormItemList.value = bpmnElementFormItemList.value.filter(item => multipleSelection.value.some(selectionItem => selectionItem.formId.toString() === item.formId.toString()));

    // 循环写入formItems节点,并且替换掉已选中表单中的formWidgets为授权后的表单部件
    multipleSelection.value.forEach(selectionItem => {
      const matchingItem = formWidgetList.value.find(widgetItem => widgetItem.formId.toString() === selectionItem.formId.toString());
      if (matchingItem) {
        selectionItem.formWidgets = [...matchingItem.formWidgets];
      }
      updateFormFields(selectionItem);
    });
    formOpen.value = false;
  }
}

// 初始化表单字段权限
function initFormFields(item) {
  const { formId, formName, formType, formWidgets } = item;
  // 新建属性字段
  const newPropertyObject = modelerStore.moddle.create(`flowable:FormItem`, { formId, formName, formType, formWidgets: JSON.stringify(formWidgets) });
  // 新建一个属性字段的保存列表
  const propertiesObject = modelerStore.moddle.create(`flowable:FormItems`, {
    values: [newPropertyObject]
  });
  updateElementExtensions(propertiesObject);
}

// 更新表单字段权限
function updateFormFields(item) {
  const { formId, nodeId, formName, formType, formWidgets } = item;
  // 创建新的属性字段对象
  const newPropertyObject = modelerStore.moddle.create(`flowable:FormItem`, { formId, nodeId, formName, formType, formWidgets: JSON.stringify(formWidgets) });
  // 筛选出 bpmnElementFormItemList 中 formId 不等于 newPropertyObject.formId 的元素
  bpmnElementFormItemList.value = bpmnElementFormItemList.value.filter(item => item.formId !== newPropertyObject.formId)
  // 将新的属性字段对象添加到 bpmnElementFormItemList 中
  bpmnElementFormItemList.value.push(newPropertyObject);
  // 创建包含所有属性字段的对象
  const propertiesObject = modelerStore.moddle.create(`flowable:FormItems`, {
    values: bpmnElementFormItemList.value
  });
  // 更新元素的扩展属性
  updateElementExtensions(propertiesObject);

  // TODO 打开注释后会同步更新后续用户任务扩展属性
  // updateAfterElementFormExtensions(userTasksAfterElement.value, formId, newPropertyObject);
}


// 移除表单字段权限
function removeFormItem() {
  const formKey = modelerStore.getBpmnElement.businessObject.formKey;  // 存储formKey属性的值
  proxy.$confirm("确认移除表单吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消",
    type: 'warning',
    center: true
  }).then(() => {
    deleteFormKey(); // 移除formKey属性
    deleteFormWidget(formKey); // 从列表中移除表单部件
    deleteMultipleSelection(formKey); // 从选择列表中移除相关项
    deleteBpmnElementFormInfo(formKey);  // 删除已配置的表单信息
  }).catch(() => {
    bpmnFormData.value.formKey = formKey;  // 恢复原始的formKey值
  });
}

// 删除formKey属性
function deleteFormKey() {
  delete modelerStore.getBpmnElement.businessObject['formKey'];
}

// 删除表单部件
function deleteFormWidget(formKey) {
  const widgetIndex = findFormWidgetIndex(formKey);
  if (widgetIndex !== -1) {
    formWidgetList.value.splice(widgetIndex, 1);
  }
}

// 删除多选项配置
function deleteMultipleSelection(formKey) {
  const selectIndex = findSelectionIndex(formKey);
  if (selectIndex !== -1) {
    multipleSelection.value.splice(selectIndex, 1);
  }
}

// 删除配置的表单信息
function deleteBpmnElementFormInfo(formKey) {
  const index = findElementFormItemIndex(formKey);
  if (index !== -1) {
    bpmnElementFormItemList.value.splice(index, 1);
    if (bpmnElementFormItemList.value.length === 0) {
      deleteCurrentElementFormExtensions(modelerStore.getBpmnElement);
    } else {
      const propertiesObject = modelerStore.moddle.create('flowable:FormItems', {values: bpmnElementFormItemList.value});
      updateElementExtensions(propertiesObject);
    }
    // 变更或删除当前节点绑定的表单后,同时删除后续其他节点的引用,需要重新授权添加
    deleteAfterElementFormExtensions(formKey);
  }
}

// 删除当前任务节点下的表单扩展属性
function deleteCurrentElementFormExtensions(element) {
  if (element.businessObject?.extensionElements?.values) {
    element.businessObject.extensionElements.values.forEach((ex, index) => {
      if (ex.$type === 'flowable:FormItems') {
        element.businessObject.extensionElements.values.splice(index, 1);
      }
    });
  }
}

// 删除其他任务节点中引用的扩展表单
function deleteAfterElementFormExtensions(formKey) {
  userTasksAfterElement.value.forEach(afterElement => {
    const { businessObject } = afterElement;
    if (businessObject?.extensionElements?.values) {
      businessObject.extensionElements.values.forEach((ex, index) => {
        if (ex.$type === 'flowable:FormItems') {
          ex.values.forEach((item, itemIndex) => {
            if (item.formId.toString() === formKey && item.nodeId === props.id) {
              businessObject.extensionElements.values[index].values.splice(itemIndex, 1);
            }
          });
        }
      });
    }
  });
}

// 更新其他任务节点中引用的扩展表单
function updateAfterElementFormExtensions(elements, formKey, properties) {
  elements.forEach(afterElement => {
    if (afterElement.businessObject?.extensionElements?.values) {
      afterElement.businessObject.extensionElements.values.forEach((ex, index) => {
        if (ex.$type === 'flowable:FormItems') {
          ex.values.forEach((item, itemIndex) => {
            if (item.formId.toString() === formKey.toString()) {
              afterElement.businessObject.extensionElements.values[index].values[itemIndex] = properties;
            }
          });
        }
      });
    }
  });
}

// 更新xml扩展属性
function updateElementExtensions(properties) {
  const extensions = modelerStore.moddle.create("bpmn:ExtensionElements", {
    values: otherExtensionList.value.concat([properties])
  });
  modelerStore.modeling.updateProperties(bpmnElement.value, {
    extensionElements: extensions
  });
}

// 查看表单
function handleShowForm(form) {
  const findForm = formList.value.find(item => item.formId === form.formId.toString());
  if (findForm) {
    viewForm.value = findForm;
    viewOpen.value = true;
    if (findForm.formType === 'route') {
      renderForm.value = false;
      formCompName.value = findForm.formContent;
      showFormWidgets(form);
    } else {
      renderForm.value = true;
      proxy.$nextTick(() => {
        vFormRef.value.setFormJson(findForm.formContent);
        // vFormRef.value.disableForm();  // 如果需要禁用整个表单，可以在这里调用
        showFormWidgets(form);
      });
    }
  }
}

// 显示表单组件
function showFormWidgets(form) {
  proxy.$nextTick(() => {
    const widgets = form.formWidgets || [];
    const widgetMap = {
      readonly: [],
      readwrite: [],
      invisible: []
    };
    widgets.forEach(widget => {
      widgetMap[widget.type].push(widget.id);
    });
    vFormRef.value.disableWidgets(widgetMap.readonly);
    vFormRef.value.enableWidgets(widgetMap.readwrite);
    vFormRef.value.hideWidgets(widgetMap.invisible);
  });
}

// 处理表单组件
function processWidgets(form){
  const formWidgets = JSON.parse(form.formWidgets);
  // 记录原组件读取状态,便于切换
  formWidgets.forEach(widget => {
    widget.oldType = widget.type;
  });
  formWidgetList.value.push({
    formId: form.formId,
    nodeId: form.nodeId,
    formName: form.formName,
    formType: form.formType,
    formWidgets: formWidgets
  });
}

// 获取标签提示信息
function getTooltipContent(type) {
  switch (type) {
    case 'invisible':
      return '不可见';
    case 'readonly':
      return '只读';
    case 'readwrite':
      return '可读写';
    default:
      return '';
  }
}

// 获取标签图标
function getIconClass(type) {
  switch (type) {
    case 'invisible':
      return 'Hide';
    case 'readonly':
      return 'View';
    case 'readwrite':
      return 'Edit';
    default:
      return '';
  }
}

// 获取标签类型
function getTagType(type) {
  switch (type) {
    case 'invisible':
      return 'danger';
    case 'readonly':
      return 'info';
    case 'readwrite':
      return 'success';
    default:
      return 'primary';
  }
}

// 标签点击事件 type: invisible:不可见 readonly:只读 readwrite:可读写
function handleTagClick(nodeId, widget) {
  if (firstUserTask.value && nodeId === 'init') {
    proxy.$modal.msgWarning('用户发起流程节点不允许更改初始表单权限！');
    return;
  }
  const {type, oldType, required} = widget;
  if (type === 'readwrite') {
    // 如果是当前节点的表单 设置组件为只读时,校验是否是必填字段,如果是,则不允许设置为只读
    if (nodeId === props.id && required && oldType !== 'readonly') {
      proxy.$modal.msgWarning('该字段为必填字段，当前节点只能设置为读写权限！');
      return;
    }
    widget.type = 'readonly';
  } else if (type === 'readonly') {
    // 如果是当前节点的表单
    if (nodeId === props.id) {
      // 只有在原来是只读时才允许设置为可读写
      widget.type = oldType === 'readonly' ? 'readwrite' : 'invisible';
    } else {
      widget.type = 'invisible';
    }
  } else {
    widget.type = type === 'invisible' ? 'readwrite' : 'invisible';
  }
}


// 表单授权
function handleRadioChange(data) {
  const { nodeId, widgetType, formWidgets } = data;
  // 批量设置组件为只读时,校验是否是当前节点的表单信息和必填字段,如果是,则不允许设置为只读
  formWidgets.forEach(widget => {
    if (widgetType !== "readonly" || nodeId !== props.id || !widget.required || widget.oldType === "readonly") {
      widget.type = widgetType;
    }
  });
}

// 禁用多选按钮
function selectable(row, index) {
  if (firstUserTask.value) {
    return row.nodeId !== 'init';
  } else {
    return true;
  }
}

// 保存选中的数据id,row-key就是要指定一个key标识这一行的数据
function getRowKey(row) {
  return row.index
}

</script>

<style scoped>

.clickable:hover {
  cursor: pointer;
}

.custom-badge {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.custom-badge-stars {
  position: relative;
}

.custom-badge-stars::before {
  content: "*";
  color: #ff4d4f;
  font-size: 18px;
  position: absolute;
  top: 80%;
  transform: translate(-50%, -50%);
}

.custom-tag-stars {
  position: relative;
  margin-left: 5px;
}

.custom-tag-stars::before {
  content: "*";
  color: #ff4d4f;
  font-size: 16px;
  position: absolute;
  top: 60%;
  transform: translate(-50%, -50%);
}

.custom-badge-text {
  font-size: 12px;
  line-height: 1.5715;
  margin-left: 4px;
  margin-right: 6px;
}

.custom-badge.info {
  background-color: #818080;
}

.custom-badge.success {
  background-color: #52c41a;
}

.custom-badge.warning {
  background-color: #faad14;
}

.custom-badge.danger {
  background-color: #ff4d4f;
}

.top-right-status {
    margin-left: auto;
    position: relative;
    float: right;
}

.inner {
  display: inline-block;
  margin: 5px; /* 可选，设置标签之间的间距 */
}

.tag-disabled {
  pointer-events: none; /* 禁用鼠标事件 */
  opacity: 0.6; /* 改变透明度以表示禁用状态 */
  cursor: not-allowed;
}

</style>
