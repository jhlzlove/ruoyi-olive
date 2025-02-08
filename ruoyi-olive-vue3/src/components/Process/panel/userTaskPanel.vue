<template>
  <el-form label-width="80px" status-icon>
<!--    <el-form-item label="异步">-->
<!--      <el-switch v-model="bpmnFormData.async" active-text="是" inactive-text="否" @change="updateElementTask('async')"/>-->
<!--    </el-form-item>-->
    <el-form-item label="审批类型">
      <el-radio-group v-model="bpmnFormData.assignType" @change="changeAssignType" :disabled="firstUserTask || multiInstanceTask">
        <el-radio v-for="item in assignTypeOption" :key="item.key" :label="item.key">{{item.label}}</el-radio >
      </el-radio-group>
    </el-form-item>

    <el-form-item label-width="15px" style="margin-bottom: 5px">
      <el-button-group class="ml-4">
        <template v-for="button in assignButtonList" :key="button.assignType">
          <el-button v-show="button.show || button.assignType === bpmnFormData.assignType" :disabled="firstUserTask || multiInstanceTask"
                     :type="button.buttonType" :icon="button.icon" size="small" class="custom-button-size" @click="handleAssignButtonClick(button.assignType)">
            {{ button.text }}
          </el-button>
        </template>
      </el-button-group>
    </el-form-item>
    <el-form-item label-width="15px" style="margin-bottom: 5px">
      <template v-if="expType === 'writeExp'" >
        <div class="inline-box">
          <div v-for="(assign, index) in selectAssignList" :key="index">
            <el-input
                v-if="editTag[index]"
                v-model="assign.value"
                size="small"
                :ref="'editInput' + index"
                @keyup.enter.native="handleEditableInputConfirm(assign, index)"
                @change="handleEditableInputConfirm(assign, index)"
                @blur="handleEditableInputBlur(assign, index)"
            ></el-input>
            <el-tag
                v-else
                style="margin-right: 5px;"
                :closable="!firstUserTask && !multiInstanceTask"
                effect="plain"
                @click="showEditTagInput(index)"
                @close="handleAssignClose(assign)"
            >
              {{assign.value}}
            </el-tag>
          </div>
        </div>
      </template>
      <template v-else>
        <el-tag
            style="margin-right: 5px;"
            v-for="assign in selectAssignList"
            :key="assign.id"
            :closable="!firstUserTask && !multiInstanceTask"
            effect="plain"
            @close="handleAssignClose(assign)"
        >
          {{assign.name}}
        </el-tag>
      </template>
    </el-form-item>
    <el-divider v-if="!firstUserTask"/>
    <el-form-item label="按钮配置" v-show="!firstUserTask">
      <el-checkbox-group v-model="bpmnFormData.buttonList" @change="handleCheckedButtonsChange">
        <el-checkbox v-for="button in buttonOption" :label="button.id" :key="button.id">
          <span> {{ button.label }}</span>
          <span style="color: #696767;padding-left: 3px" @click.stop.prevent="editButton(button)">
            <el-icon><EditPen /></el-icon>
          </span>
        </el-checkbox>
      </el-checkbox-group>
    </el-form-item>

    <!--   抄送信息     -->
    <copy-panel :id="id"  v-show="!firstUserTask"/>

    <el-form-item label="退回设置" label-width="95px" v-if="!firstUserTask && !multiInstanceTask">
      <el-switch
          class="custom-switch"
          @change="updateElementTask('autoSkip')"
          v-model="bpmnFormData.autoSkip"
          active-text="重新提交跳过中间节点"
          inactive-text="重新提交不跳过中间节点"
          active-value="true"
          inactive-value="false"
          inline-prompt>
      </el-switch>
      <el-tooltip placement="top" content="a,b,c三个节点, c退回至a, a节点重新提交任务后直接到c节点">
        <el-icon><Warning /></el-icon>
      </el-tooltip>
    </el-form-item>
<!--    <el-form-item label="优先级">-->
<!--      <el-input v-model="bpmnFormData.priority" @change="updateElementTask('priority')"/>-->
<!--    </el-form-item>-->
    <el-form-item label-width="95px" v-if="!firstUserTask && !multiInstanceTask">
      <template #label>
        <span>
           到期时间
           <el-tooltip placement="top">
              <template #content>
                 <div>
                   dueDate属性只是标识该用户任务何时过期，但过期后不会自动完成。
                   <br/>
                   在flowable中，用户任务过期时间存储在 ACT_RU_TASK 表的 DUE_DATE 字段中，
                   <br/>
                   flowable提供了基于过期时间进行查询的API。
                 </div>
              </template>
              <el-icon><QuestionFilled /></el-icon>
           </el-tooltip>
        </span>
      </template>
      <el-select v-model="bpmnFormData.timerType" class="m-2 select-margin" placeholder="请选择到期时间类型" clearable @change="handleTimerChange" @clear="handleClearDueDate">
        <el-option
            v-for="item in timerList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
      <template v-if="bpmnFormData.timerType === 'timeDuration'">
        <!--到期时间设置-->
        <el-popover placement="top" trigger="click" title="到期时间设置" width="320px" append-to-body>
          <el-input-number controls-position="right" data-unit="年" :min="0" v-model="bpmnFormData.timeDuration.years" class="my-el-input-number" @change="changeTimeDuration" />
          <el-input-number controls-position="right" data-unit="月" :min="0" v-model="bpmnFormData.timeDuration.months" class="my-el-input-number" @change="changeTimeDuration" />
          <el-input-number controls-position="right" data-unit="日" :min="0" v-model="bpmnFormData.timeDuration.days" class="my-el-input-number" @change="changeTimeDuration"/>
          <br/>
          <el-input-number controls-position="right" data-unit="时" :min="0" v-model="bpmnFormData.timeDuration.hours" class="my-el-input-number" @change="changeTimeDuration"/>
          <el-input-number controls-position="right" data-unit="分" :min="0" v-model="bpmnFormData.timeDuration.minutes" class="my-el-input-number" @change="changeTimeDuration"/>
          <el-input-number controls-position="right" data-unit="秒" :min="0" v-model="bpmnFormData.timeDuration.seconds" class="my-el-input-number" @change="changeTimeDuration"/>

          <template #reference>
            <el-input v-model="bpmnFormData.dueDate" placeholder="请填写到期时间">
              <template #append>
                <el-button icon="Setting" ></el-button>
              </template>
            </el-input>
          </template>
        </el-popover>
      </template>
      <template v-else-if="bpmnFormData.timerType === 'timeDate'">
        <el-date-picker v-model="bpmnFormData.dueDate" format="YYYY-MM-DDTHH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" placeholder="请选择到期时间" @change="updateElementTask('dueDate')"/>
      </template>
      <template v-else>
        <el-input v-model="bpmnFormData.dueDate" placeholder="请填写表达式" @change="updateElementTask('dueDate')"/>
      </template>
    </el-form-item>
    <el-form-item label="跳过任务" label-width="95px" v-if="!firstUserTask && !multiInstanceTask">
      <el-input v-model="bpmnFormData.skipExpression" placeholder="示例: ${assignee == 'leader'}" @change="updateElementTask('skipExpression')"/>
    </el-form-item>
  </el-form>

  <!--更新按钮信息-->
  <el-dialog title="按钮配置" v-model="buttonDialogVisible" width="500px" append-to-body>
    <el-form :model="buttonForm" label-width="80px">
      <el-form-item label="按钮标签" prop="label">
        <el-input v-model="buttonForm.label" placeholder="请输入按钮标签" />
      </el-form-item>
      <el-form-item label="按钮图标" prop="icon">
        <el-select filterable placeholder="请选择图标" v-model="buttonForm.icon">
          <el-option
              v-for="item in iconList"
              :key="item.name"
              :label="item.name"
              :value="item.name"
          >
            <el-icon style="float: left; margin-top: 10px;">
              <component :is="item.name" />
            </el-icon>
            <span style="float: right; color: #8492a6; font-size: 11px">{{ item.name }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="按钮类型" prop="type">
        <el-select v-model="buttonForm.type" placeholder="请选择按钮类型">
          <el-option
              v-for="(item , index) in buttonTypeOptions"
              :key="index"
              :label="item"
              :value="item"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="按钮尺寸" prop="size">
        <el-select v-model="buttonForm.size" placeholder="请选择按钮尺寸">
          <el-option
              v-for="(item , index) in buttonSizeOptions"
              :key="index"
              :label="item"
              :value="item"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
        <span class="dialog-footer">
      <el-button @click="buttonDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitButton">确 定</el-button>
       </span>
    </template>
  </el-dialog>

  <!--流程任务接收-->
  <el-dialog
      title="选择任务接收"
      v-model="assignOpen"
      :width="currentType === 'dept' ? '30%' : '65%'"
      :before-close="cancelAssignSelect"
      :close-on-press-escape="false"
      :destroy-on-close="true"
      append-to-body
  >
    <component :is="currentComponent" :selectData="selectData[currentType]" :suitType="bpmnFormData.assignType" @handleAssignSelect="handleAssignSelect"/>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelAssignSelect">取 消</el-button>
        <el-button type="primary" @click="saveAssignSelect">确 认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup name="UserTaskPanel">
import FlowAssignee from '@/components/Flow/Design/Assignee';
import FlowUsers from '@/components/Flow/Design/User';
import FlowRole from '@/components/Flow/Design/Role';
import FlowExp from '@/components/Flow/Design/Expression';
import FlowDept from '@/components/Flow/Design/Dept';
import FlowPost from '@/components/Flow/Design/Post';
import {StrUtil} from '@/utils/StrUtil';
import useModelerStore from '@/store/modules/modeler';
import * as elementIcons from '@element-plus/icons-vue';
import {assignSelect} from "@/views/flowable/common/AssignUtils";
import CopyPanel from "./copyPanel.vue";
import EventBus from '@/components/Process/common/EventBus';

const modelerStore = useModelerStore()

const {proxy} = getCurrentInstance();

const editTag = ref([]);
// 表达式类型
const expType = ref('');
const assignOpen = ref(false);
const assignType = ref('');
const otherExtensionList = ref([]);
const bpmnElementButtonList = ref([]);
// 流程指定列表
const selectAssignList = ref([]);
// 选择审核数据列表
const selectDataList = ref([]);
// 按钮信息
const buttonOption = ref([]);
// 用于确定当前显示的组件
const currentType = ref("");
const currentComponent = shallowRef(null);
const buttonDialogVisible = ref(false);
// 是否是第一个用户任务节点
const firstUserTask = ref(false);
// 是否会签任务节点
const multiInstanceTask = ref(false);
const iconList = ref(elementIcons);
const data = reactive({
  assignTypeOption: [
    {label: '办理人', key: 'assignee', value: 'assignee'},
    {label: '候选人', key: 'users', value: 'candidateUsers'},
    {label: '角色', key: 'role', value: 'candidateGroups'},
    {label: '部门', key: 'dept', value: 'candidateGroups'},
    {label: '岗位', key: 'post', value: 'candidateGroups'},
  ],
  bpmnFormData: {
    assignType: "assignee",
    assignee: null,
    candidateUsers: null,
    candidateGroups: null,
    priority: "",
    dataType: "",
    expId: "",
    writeExp: false,
    buttonList: [],
    $type: "",
    autoSkip: "false",
    timerType: 'timeDate',
    skipExpression: "",
    dueDate: "",
    timeDuration: {
      years: undefined,
      months: undefined,
      days: undefined,
      hours: undefined,
      minutes: undefined,
      seconds: undefined,
    },
  },
  // 数据回显
  selectData: {
    assignee: null,
    users: null,
    role: null,
    dept: null,
    post: null,
    exp: null,
  },
  // 按钮编辑信息
  buttonForm: {},
  buttonSizeOptions: ['large', 'default', 'small'],
  buttonTypeOptions: ['primary', 'default', 'success', 'info', 'warning', 'danger'],
  selectExpData: {}, // 选择表达式数据
  assignButtonList: [
    {assignType: 'assignee', text: '指定用户', show: false, buttonType: 'primary', icon: 'User'},
    {assignType: 'users', text: '候选用户', show: false, buttonType: 'primary', icon: 'User'},
    {assignType: 'role', text: '候选角色', show: false, buttonType: 'primary', icon: 'User'},
    {assignType: 'dept', text: '候选部门', show: false, buttonType: 'primary', icon: 'User'},
    {assignType: 'post', text: '候选岗位', show: false, buttonType: 'primary', icon: 'User'},
    {assignType: 'exp', text: '内置表达式', show: true, buttonType: 'warning', icon: 'Postcard'},
    {assignType: 'writeExp', text: '自定义表达式', show: true, buttonType: 'info', icon: 'Postcard'},
  ],
  startTaskAssign: {
    id: 1,
    name: '流程发起人',
    expression: '${INITIATOR}',
    dataType: 'fixed',
    assignType: 'assignee',
  },
  multiInstanceAssign: {
    id: 999,
    name: '会签人',
    dataType: 'fixed',
    assignType: 'assignee',
  },
  timerList: [
    {label: '表达式', value: 'timeExp'},
    {label: '指定时间', value: 'timeDate'},
    {label: '等待时长', value: 'timeDuration'},
  ],
});

const {
  assignTypeOption,
  assignButtonList,
  bpmnFormData,
  selectData,
  buttonForm,
  buttonSizeOptions,
  buttonTypeOptions,
  selectExpData,
  startTaskAssign,
  multiInstanceAssign,
  timerList
} = toRefs(data);

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
        resetTaskForm();
      }
    },
    {immediate: true}
)

onMounted(() => {
  EventBus.on('multiInstance-updated', (value) => {
    console.log('multiInstance-updated', value);
    multiInstanceTask.value = value;
    if (value){
      deleteFlowAttar();
      selectAssignList.value = [{id: multiInstanceAssign.value.id, name: multiInstanceAssign.value.name}];
      updateCustomElement('assignType', multiInstanceAssign.value.assignType);
      updateCustomElement('dataType', multiInstanceAssign.value.dataType);
      updateCustomElement('expId', multiInstanceAssign.value.id);
      const expression = "${" + props.id + "_assignee}";
      updateAssignElement(multiInstanceAssign.value.assignType, expression);
      handleSelectData("exp", multiInstanceAssign.value.id);
      bpmnFormData.value.assignType = multiInstanceAssign.value.assignType;
    } else {
      deleteFlowAttar();
      selectAssignList.value = [];
    }
  });
})

// 初始化表单
function resetTaskForm() {
  const businessObject = modelerStore.getBpmnElement?.businessObject;
  buttonOption.value = JSON.parse(JSON.stringify(modelerStore.getButtonOption));
  // 初始化设为空值
  bpmnFormData.value = {
    assignType: "assignee",
    assignee: null,
    candidateUsers: null,
    candidateGroups: null,
    priority: "",
    dataType: "",
    expId: "",
    writeExp: false,
    buttonList: [],
    $type: "",
    autoSkip: "false",
    timerType: 'timeDate',
    skipExpression: "",
    dueDate: "",
    timeDuration: {
      years: undefined,
      months: undefined,
      days: undefined,
      hours: undefined,
      minutes: undefined,
      seconds: undefined,
    },
  }
  selectData.value = {
    assignee: null,
    users: null,
    role: null,
    dept: null,
    post: null,
    exp: null,
  }
  expType.value = '';
  selectAssignList.value = [];
  multiInstanceTask.value = false;
  // 流程节点信息上取值
  for (let key in bpmnFormData.value) {
    const value = businessObject[key] || bpmnFormData.value[key];
    Reflect.set(bpmnFormData.value, key, value);
  }

  // 过期时间回显
  getDueDateType(bpmnFormData.value);

  // 按钮回显
  resetButtonList();
  updateCustomElement('assignType', bpmnFormData.value.assignType);

  // 如果是第一个任务节点，设置默认审批人为流程发起人
  checkFirstUserTask(businessObject);
  if (firstUserTask.value){
    selectAssignList.value = [{id: startTaskAssign.value.id, name: startTaskAssign.value.name}];
    updateCustomElement('dataType', startTaskAssign.value.dataType);
    updateCustomElement('expId', startTaskAssign.value.id.toString());
    updateAssignElement(startTaskAssign.value.assignType, startTaskAssign.value.expression);
    handleSelectData("exp", startTaskAssign.value.id);
  }
  // 会签节点
  if (businessObject.loopCharacteristics){
    multiInstanceTask.value = true;
    selectAssignList.value = [{id: multiInstanceAssign.value.id, name: multiInstanceAssign.value.name}];
    updateCustomElement('dataType', multiInstanceAssign.value.dataType);
    updateCustomElement('expId', multiInstanceAssign.value.id);
    const expression = "${" + props.id + "_assignee}";
    updateAssignElement(multiInstanceAssign.value.assignType, expression);
    handleSelectData("exp", multiInstanceAssign.value.id);
  }

  // 人员信息回显
  if (!multiInstanceTask.value && !firstUserTask.value){
    checkValuesEcho(bpmnFormData.value);
  }
}

// 更新节点信息
function updateElementTask(key) {
  const taskAttr = Object.create(null);
  taskAttr[key] = bpmnFormData.value[key] || "";
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 检验是否是第一个任务节点
function checkFirstUserTask(businessObject) {
  const process = businessObject.$parent;
  const isUserTask = businessObject.$type === 'bpmn:UserTask';

  if (process.routineTask) {
    const firstTask = process.flowElements.find(element => element.$type === 'bpmn:UserTask');
    firstUserTask.value = !firstTask || (isUserTask && businessObject.id === firstTask.id);
  } else {
    firstUserTask.value = false;
  }
}

// 更新自定义流程节点/参数信息
function updateCustomElement(key, value) {
  const taskAttr = Object.create(null);
  taskAttr[key] = value;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 更新审批人流程节点/参数信息
function updateAssignElement(assignType, value) {
  const key = getAssignValueByKey(assignType);
  const taskAttr = Object.create(null);
  taskAttr[key] = value;
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, taskAttr);
}

// 更新人员类型
function changeAssignType(val) {
  // 删除xml中已选择数据类型节点
  deleteFlowAttar();
  // delete modelerStore.getBpmnElement.businessObject[`assignType`]
  // 清除已选人员数据
  bpmnFormData.value[val] = null;
  selectAssignList.value = [];
  selectData.value = {
    assignee: null,
    users: null,
    role: null,
    dept: null,
    post: null,
    exp: null,
  }
  // 写入assignType节点信息到xml
  updateCustomElement('assignType', val);
}

// 设计器右侧表单数据回显,检查表单数据并回显
function checkValuesEcho(formData) {
  if (formData.writeExp) {
    const assignValue = getAssignValueByKey(formData.assignType)
    const expValue = formData[assignValue];
    expType.value = 'writeExp';
    selectAssignList.value = [{value: expValue}];
  } else {
    if (StrUtil.isNotBlank(formData.expId)) {
      getExpList(formData.expId);
    } else {
      if (["role", "dept", "post"].includes(formData.assignType)) {
        getListByType(formData, `get${capitalizeFirstLetter(formData.assignType)}List`);
      } else {
        getUserList(formData);
      }
    }
  }
}

// 获取表达式信息
function getExpList(val) {
  if (StrUtil.isNotBlank(val)) {
    // 根据id查找表达式名称
    const expData = modelerStore.getExpList?.find(item => item.id.toString() === val);
    selectAssignList.value = [{id: expData.id, name: expData.name}];
    // 设置选中的表达式id
    selectData.value.exp = expData.id;
  }
}

// 获取人员信息
function getUserList(formData) {
  const assignValue = getAssignValueByKey(formData.assignType)
  let val = formData[assignValue];
  if (StrUtil.isNotBlank(val)) {
    if (typeof val === "number") {
      val = val.toString();
    }
    const newArr = modelerStore.getUserList?.filter(i => val.split(',').includes(i.userId.toString()))
    updateSelectAssignList(newArr, "userId", "nickName", formData.assignType);

  }
}

// 统一的获取信息方法
function getListByType(formData, listType) {
  const assignValue = getAssignValueByKey(formData.assignType);
  let val = formData[assignValue];
  if (StrUtil.isNotBlank(val)) {
    if (typeof val === "number") {
      val = val.toString();
    }
    const newArr = modelerStore[listType]?.filter(i => val.split(',').includes(i[`${formData.assignType}Id`].toString()));
    updateSelectAssignList(newArr, `${formData.assignType}Id`, `${formData.assignType}Name`, formData.assignType);
  }
}

// 更新选择的分配列表
function updateSelectAssignList(data, idKey, nameKey, assignType) {
  selectAssignList.value = data.map(item => ({ id: item[idKey], name: item[nameKey] }));
  if ("exp" === assignType || "assignee" === assignType || "dept" === assignType) {
    selectData.value[assignType] = data.map(item => item[idKey]).join(',');
  } else {
    selectData.value[assignType] = data;
  }
}

// 首字母大写
function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

// 处理信息选中数据
function handleAssignSelect(selection) {
  if (selection) {
    if (currentType.value === "exp") {
      // 处理表达式选中数据
      selectExpData.value = JSON.parse(JSON.stringify(selection));
    } else {
      // 处理审批选中数据
      selectDataList.value = JSON.parse(JSON.stringify(selection));
    }
  }
}

// 处理审批按钮选中后切换不同组件
function handleAssignButtonClick(assignType) {
  if (assignType === 'writeExp') {
    deleteAssignAttar();
    expType.value = assignType;
    const expValue = "${" + props.id + "_assignee}";
    selectAssignList.value = [{value: expValue}];
    updateCustomElement('writeExp', true);
    updateCustomElement('dataType', 'dynamic');
    updateAssignElement(bpmnFormData.value.assignType, expValue);
  } else {
    currentType.value = assignType;
    // 根据不同的 assignType，设置不同的组件
    switch (assignType) {
      case 'assignee':
        currentComponent.value = FlowAssignee;
        break;
      case 'users':
        currentComponent.value = FlowUsers;
        break;
      case 'role':
        currentComponent.value = FlowRole;
        break;
      case 'dept':
        currentComponent.value = FlowDept;
        break;
      case 'post':
        currentComponent.value = FlowPost;
        break;
      case 'exp':
        currentComponent.value = FlowExp;
        break;
      default:
        currentComponent.value = null;
    }
    assignOpen.value = true;
  }
}


// 保存选中的数据
function saveAssignSelect() {
  if (currentType.value === "exp") {
    const selection = selectExpData.value
    if (selection.id) {
      selectAssignList.value = [{id: selection.id, name: selection.name}];
      updateCustomElement('writeExp', false);
      updateCustomElement('dataType', selection.dataType);
      updateCustomElement('expId', selection.id.toString());
      updateAssignElement(bpmnFormData.value.assignType, selection.expression);
      handleSelectData("exp", selection.id);
    }
  } else {
    updateCustomElement('dataType', 'fixed');
    selectAssignList.value = JSON.parse(JSON.stringify(assignSelect(bpmnFormData.value.assignType, "", selectDataList.value)));
    // 更新xml数据
    updateAssignElement(bpmnFormData.value.assignType, selectAssignList.value.map(item => item.id).join(',') || '');
    if ("assignee" === bpmnFormData.value.assignType || "dept" === bpmnFormData.value.assignType) {
      handleSelectData(bpmnFormData.value.assignType, selectAssignList.value.map(item => item.id).join(',') || '');
    } else {
      handleSelectData(bpmnFormData.value.assignType, selectAssignList.value);
    }
  }
  expType.value = '';
  cancelAssignSelect();
}

// 取消任务审批数据
function cancelAssignSelect() {
  assignOpen.value = false;
}


// 处理人员回显
function handleSelectData(key, value) {
  for (let oldKey in selectData.value) {
    if (key !== oldKey) {
      Reflect.set(selectData.value, oldKey, null);
    } else {
      Reflect.set(selectData.value, oldKey, value);
    }
  }
}

/*根据选择的key获取选中指定审批类型的value*/
function getAssignValueByKey(key) {
  if (StrUtil.isBlank(key)) {
    return "";
  }
  return assignTypeOption.value.find(item => item.key === key).value;
}


/*移除选的审批人*/
function handleAssignClose(assign) {
  const index = selectAssignList.value.findIndex(item => item.id === assign.id);
  if (index !== -1) {
    selectAssignList.value.splice(index, 1);
    updateAssignElement(bpmnFormData.value.assignType, selectAssignList.value.map(item => item.id).join(',') || '');
    if ("assignee" === bpmnFormData.value.assignType || "dept" === bpmnFormData.value.assignType) {
      handleSelectData(bpmnFormData.value.assignType, selectAssignList.value.map(item => item.id).join(',') || '');
    } else {
      handleSelectData(bpmnFormData.value.assignType, selectAssignList.value);
    }
  }
}

// 删除节点
function deleteFlowAttar() {
  delete modelerStore.getBpmnElement.businessObject[`dataType`]
  delete modelerStore.getBpmnElement.businessObject[`assignType`]
  delete modelerStore.getBpmnElement.businessObject[`expId`]
  delete modelerStore.getBpmnElement.businessObject[`assignee`]
  delete modelerStore.getBpmnElement.businessObject[`candidateUsers`]
  delete modelerStore.getBpmnElement.businessObject[`candidateGroups`]
  delete modelerStore.getBpmnElement.businessObject[`autoSkip`]
  delete modelerStore.getBpmnElement.businessObject[`writeExp`]
}

function deleteAssignAttar() {
  delete modelerStore.getBpmnElement.businessObject[`expId`]
  delete modelerStore.getBpmnElement.businessObject[`assignee`]
  delete modelerStore.getBpmnElement.businessObject[`candidateUsers`]
  delete modelerStore.getBpmnElement.businessObject[`candidateGroups`]
}


function resetButtonList() {
  otherExtensionList.value = []; // 其他扩展配置
  const bpmnElementProperties =
      modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => {
        if (ex.$type !== `flowable:Buttons`) {
          otherExtensionList.value.push(ex);
        }
        if (ex.values) {
          return ex.$type === `flowable:Buttons`;
        }
      }) ?? [];

  if (bpmnElementProperties.length > 0) {
    // 保存所有的 扩展按钮字段
    bpmnElementButtonList.value = bpmnElementProperties.reduce((pre, current) => pre.concat(current.values), []);
    // 复制 显示
    const elementButtonList = JSON.parse(JSON.stringify(bpmnElementButtonList.value ?? []));
    // 读取节点上已配置的按钮节点
    buttonOption.value.forEach((button, index, arr) => {
      const elButton = elementButtonList.find(element => element.id === button.id)
      if (elButton) {
        arr[index] = elButton;
      }
    })
    bpmnFormData.value.buttonList = elementButtonList.map(item => item.id);
  }
}

function handleCheckedButtonsChange(values) {
  if (values.length > 0) {
    // 当前选中的按钮
    const checkedButtons = buttonOption.value?.filter(i => values.includes(i.id))
    const newButtonList = [];
    checkedButtons.forEach(button => {
      const { id, label, code, icon, type, size, click } = button;
      const newButtonObject = modelerStore.getModdle.create(`flowable:Button`, { id, label, code, icon, type, size, click});
      newButtonList.push(newButtonObject);
    })
    const buttonsObject = modelerStore.getModdle.create(`flowable:Buttons`, {
      values: newButtonList
    });
    updateElementExtensions(buttonsObject);
    resetButtonList();
  } else {
    const buttonsObject = modelerStore.getModdle.create(`flowable:Buttons`, {
      values: []
    });
    updateElementExtensions(buttonsObject);
  }
}

// 更新扩展属性信息
function updateElementExtensions(properties) {
  const extensions = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: otherExtensionList.value.concat([properties])
  });

  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    extensionElements: extensions
  });
}

// 修改按钮信息
function editButton(button) {
  buttonDialogVisible.value = true;
  buttonForm.value = JSON.parse(JSON.stringify(button));
}

// 更新按钮信息
function submitButton() {
  // 找到下标后更改数据
  const index = buttonOption.value.findIndex(item => item.id === buttonForm.value.id);
  buttonOption.value[index] = buttonForm.value;
  // 替换选中的按钮
  const checkedButtons = buttonOption.value?.filter(i => bpmnFormData.value.buttonList.includes(i.id))
  const newButtonList = [];
  for (let button in checkedButtons) {
    const { id, label, code, icon, type, size, click} = checkedButtons[button];
    const newButtonObject = modelerStore.getModdle.create(`flowable:Button`, { id, label, code, icon, type, size, click});
    newButtonList.push(newButtonObject);
  }
  const buttonsObject = modelerStore.getModdle.create(`flowable:Buttons`, {
    values: newButtonList
  });
  updateElementExtensions(buttonsObject);
  resetButtonList();
  buttonDialogVisible.value = false;
  buttonForm.value = {};
}

//编辑 input显示
function showEditTagInput(index) {
  Reflect.set(editTag.value, index, true);
  proxy.$nextTick((_) => {
    const editInput = "editInput" + index;
    proxy.$refs[editInput][0].$refs.input.focus();
  });
}

//编辑 input发生改变
function handleEditableInputConfirm(item, index) {
  if (item.value) {
    Reflect.set(editTag.value, index, false);
    selectAssignList.value = [{value: item.value}];
    updateAssignElement(bpmnFormData.value.assignType, item.value);
  } else {
    proxy.$modal.msgError( "请输入表达式");
  }
}

//编辑 input失去焦点
function handleEditableInputBlur(item, index) {
  Reflect.set(editTag.value, index, false);
}

// 设置TimeDuration时间类型
function changeTimeDuration() {
  bpmnFormData.value.dueDate = convertToISO8601(bpmnFormData.value.timeDuration);
  updateElementTask('dueDate');
}


// 切换到期时间类型
function handleTimerChange(val) {
  bpmnFormData.value.timerType = val;
  bpmnFormData.value.dueDate = '';
  delete modelerStore.getBpmnElement.businessObject[`dueDate`]
}

// 清空到期时间
function handleClearDueDate(){
  bpmnFormData.value.timerType = '';
  bpmnFormData.value.dueDate = '';
  delete modelerStore.getBpmnElement.businessObject[`dueDate`]
}

// 转为ISO8601格式
function convertToISO8601(timeObj) {
  let duration = "P";

  if (timeObj.years) {
    duration += `${timeObj.years}Y`;
  }
  if (timeObj.months) {
    duration += `${timeObj.months}M`;
  }
  if (timeObj.days) {
    duration += `${timeObj.days}D`;
  }

  // 时间
  if (timeObj.hours || timeObj.minutes || timeObj.seconds) {
    duration += "T";

    if (timeObj.hours) {
      duration += `${timeObj.hours}H`;
    }
    if (timeObj.minutes) {
      duration += `${timeObj.minutes}M`;
    }
    if (timeObj.seconds) {
      duration += `${timeObj.seconds}S`;
    }
  }

  return duration;
}

// 解析ISO8601格式
function parseISO8601(duration) {
  const regex = /P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?/;
  const matches = duration.match(regex);

  return {
    years: matches[1] ? parseInt(matches[1]) : undefined,
    months: matches[2] ? parseInt(matches[2]) : undefined,
    days: matches[3] ? parseInt(matches[3]) : undefined,
    hours: matches[4] ? parseInt(matches[4]) : undefined,
    minutes: matches[5] ? parseInt(matches[5]) : undefined,
    seconds: matches[6] ? parseInt(matches[6]) : undefined
  };
}

// 获取到期时间类型
function getDueDateType(bpmnFormData) {
  if (bpmnFormData.dueDate) {
    if (bpmnFormData.dueDate.startsWith('${')) {
      bpmnFormData.timerType = 'timeExp';
    } else if (bpmnFormData.dueDate.startsWith('P')) {
      bpmnFormData.timerType = 'timeDuration';
      bpmnFormData.timeDuration = parseISO8601(bpmnFormData.dueDate);
    } else {
      bpmnFormData.timerType = 'timeDate';
    }
  } else {
    bpmnFormData.timerType = 'timeExp';
  }
}
</script>

<style>

.inline-box {
  width: 300px;
}

.inline-box > div {
  margin-right: 5px;
}

.my-el-input-number[data-unit] {
  --el-input-number-unit-offset-x: 35px;
  position: relative;
  margin-top: 5px;
  margin-right: 5px;
  width: 90px;
}

.el-input-number .el-input__wrapper{
  padding-left: 0;
  padding-right: 0;
}

.el-input-number.is-controls-right .el-input__wrapper {
  padding-left: 0;
  padding-right: 0;
}

.my-el-input-number[data-unit]::after {
  content: attr(data-unit);
  height: 100%;
  display: flex;
  align-items: center;
  position: absolute;
  top: 0;
  right: var(--el-input-number-unit-offset-x);
  color: #999999;
}
.my-el-input-number[data-unit] .el-input__inner {
  padding-left: 20px;
  padding-right: calc(var(--el-input-number-unit-offset-x) + 12px);
}
</style>
