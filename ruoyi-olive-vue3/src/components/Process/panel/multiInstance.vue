<template>
  <div class="panel-tab__content">
    <el-form label-width="70px" @submit.native.prevent>
      <el-form-item label="参数说明">
        <el-button type="primary" size="small" class="custom-button-size" @click="dialogVisible = true">查看</el-button>
      </el-form-item>
      <el-form-item label="实例类型">
        <el-select v-model="loopCharacteristics" @change="changeLoopCharacteristicsType">
          <!--bpmn:MultiInstanceLoopCharacteristics-->
          <el-option label="并行多实例" value="ParallelMultiInstance" />
          <el-option label="串行多实例" value="SequentialMultiInstance" />
          <!--bpmn:StandardLoopCharacteristics-->
<!--          <el-option label="循环事件" value="StandardLoop" />-->
          <el-option label="无" value="Null" />
        </el-select>
      </el-form-item>
      <template v-if="loopCharacteristics === 'ParallelMultiInstance' || loopCharacteristics === 'SequentialMultiInstance'">
<!--去除手动配置任务多实例的生成次数-->
<!--        <el-form-item label="循环基数" key="loopCardinality">-->
<!--          <el-input v-model="loopInstanceForm.loopCardinality" clearable @change="updateLoopCardinality" />-->
<!--        </el-form-item>-->
        <el-form-item label="集合" key="collection">
          <el-input v-model="loopInstanceForm.collection" placeholder="示例: assigneeList" disabled @change="updateLoopBase" >
            <template #append>
              <el-button icon="RefreshLeft" @click="handleReduction"></el-button>
              <el-button style="margin-left: 1px" icon="User" @click="handleMultiSelect('users')"></el-button>
              <el-button style="margin-left: 1px" icon="Postcard" @click="handleMultiSelect('exp')"></el-button>
            </template>
          </el-input>
        </el-form-item>
<!--        <el-form-item label="元素变量" key="elementVariable">-->
<!--          <el-input v-model="loopInstanceForm.elementVariable" clearable @change="updateLoopBase" />-->
<!--        </el-form-item>-->
        <el-form-item label="通过率" key="completionCondition" class="custom-form-item">
          <el-row>
            <el-col :span="16">
              <el-progress :text-inside="true" :stroke-width="20" :percentage="loopInstanceForm.percentage" style="margin-top: 6px;"></el-progress>
            </el-col>
            <el-col :span="8">
              <el-input-number v-model="loopInstanceForm.percentage" size="small" class="custom-input-number" controls-position="right" @change="updateLoopCondition" :min="0" :max="100"></el-input-number>
            </el-col>
          </el-row>
        </el-form-item>

<!--        <el-form-item label="异步状态" key="async">-->
<!--          <el-checkbox v-model="loopInstanceForm.asyncBefore" label="异步前" @change="updateLoopAsync('asyncBefore')" />-->
<!--          <el-checkbox v-model="loopInstanceForm.asyncAfter" label="异步后" @change="updateLoopAsync('asyncAfter')" />-->
<!--          <el-checkbox-->
<!--              v-model="loopInstanceForm.exclusive"-->
<!--              v-if="loopInstanceForm.asyncAfter || loopInstanceForm.asyncBefore"-->
<!--              label="排除"-->
<!--              @change="updateLoopAsync('exclusive')"-->
<!--          />-->
<!--        </el-form-item>-->
        <el-form-item label="重试周期" prop="timeCycle" v-if="loopInstanceForm.asyncAfter || loopInstanceForm.asyncBefore" key="timeCycle">
          <el-input v-model="loopInstanceForm.timeCycle" clearable @change="updateLoopTimeCycle" />
        </el-form-item>
      </template>
    </el-form>
  </div>

  <!-- 参数说明 -->
  <el-dialog  title="多实例参数" v-model="dialogVisible" width="680px" @closed="$emit('close')">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="使用说明">按照BPMN2.0规范的要求，用于为每个实例创建执行的父执行，会提供下列变量:</el-descriptions-item>
      <el-descriptions-item label="collection(集合变量)">传入List参数, 一般为用户ID集合</el-descriptions-item>
      <el-descriptions-item label="elementVariable(元素变量)">List中单个参数的名称，指定一个别名，以备动态指定。注意设置任务处理人时，一定要与别名一致</el-descriptions-item>
      <el-descriptions-item label="loopCardinality(基数)">List循环次数</el-descriptions-item>
      <el-descriptions-item label="isSequential(串并行)">Parallel: 并行多实例，Sequential: 串行多实例</el-descriptions-item>
      <el-descriptions-item label="completionCondition(完成条件)">任务出口条件</el-descriptions-item>
      <el-descriptions-item label="nrOfInstances(实例总数)">实例总数</el-descriptions-item>
      <el-descriptions-item label="nrOfActiveInstances(未完成数)">当前活动的（即未完成的），实例数量。对于顺序多实例，这个值总为1</el-descriptions-item>
      <el-descriptions-item label="nrOfCompletedInstances(已完成数)">已完成的实例数量</el-descriptions-item>
      <el-descriptions-item label="loopCounter">给定实例在for-each循环中的index</el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <!--会签人员-->
  <el-dialog
      title="选择会签人员"
      v-model="multiOpen"
      width="65%"
      :close-on-press-escape="false"
      :destroy-on-close="true"
      append-to-body
  >
    <component :is="currentComponent" :suitType="'multi'" @handleAssignSelect="handleAssignSelect"/>
     <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelAssignSelect">取 消</el-button>
          <el-button type="primary" @click="saveAssignSelect">确 认</el-button>
        </span>
     </template>
  </el-dialog>
</template>
<script setup name="MultiInstance">
import {assignSelect} from "@/views/flowable/common/AssignUtils";
import FlowUsers from '@/components/Flow/Design/User';
import FlowExp from '@/components/Flow/Design/Expression';
import useModelerStore from '@/store/modules/modeler';
import EventBus from '@/components/Process/common/EventBus';

const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

const dialogVisible = ref(false);
const multiOpen = ref(false);
const currentType = ref("");
const selectExpData = ref({});
const selectDataList = ref([]);
const loopCharacteristics = ref("");
const loopInstanceForm = ref({
  percentage: 100,
});
const multiLoopInstance = ref({});
const currentComponent = shallowRef(null);

/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});

const data = reactive({
  defaultLoopInstanceForm: {
    completionCondition: "",
    loopCardinality: "",
    extensionElements: [],
    asyncAfter: false,
    asyncBefore: false,
    exclusive: false,
    percentage: 100,
  },
});

const { defaultLoopInstanceForm } = toRefs(data);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        getElementLoop(modelerStore.getBpmnElement.businessObject);
      }
    },
    {immediate: true}
)

// 方法区
function getElementLoop(businessObject) {
  if (!businessObject.loopCharacteristics) {
    loopCharacteristics.value = "Null";
    loopInstanceForm.value = {};
    return;
  }
  if (businessObject.loopCharacteristics.$type === "bpmn:StandardLoopCharacteristics") {
    loopCharacteristics.value = "StandardLoop";
    loopInstanceForm.value = {};
    return;
  }
  if (businessObject.loopCharacteristics.isSequential) {
    loopCharacteristics.value = "SequentialMultiInstance";
  } else {
    loopCharacteristics.value = "ParallelMultiInstance";
  }
  // 合并配置
  loopInstanceForm.value = {
    ...defaultLoopInstanceForm.value,
    ...businessObject.loopCharacteristics,
    completionCondition: businessObject.loopCharacteristics?.completionCondition?.body ?? "",
    loopCardinality: businessObject.loopCharacteristics?.loopCardinality?.body ?? "",
    percentage: extractNumberFromString(businessObject.loopCharacteristics?.completionCondition?.body),
  };
  // 保留当前元素 businessObject 上的 loopCharacteristics 实例
  multiLoopInstance.value = modelerStore.getBpmnElement.businessObject.loopCharacteristics;
  // 更新表单
  if (
      businessObject.loopCharacteristics.extensionElements &&
      businessObject.loopCharacteristics.extensionElements.values &&
      businessObject.loopCharacteristics.extensionElements.values.length
  ) {
    Reflect.set(loopInstanceForm.value, "timeCycle", businessObject.loopCharacteristics.extensionElements.values[0].body);
  }
}

function changeLoopCharacteristicsType(type) {
  // 切换类型取消原表单配置
   loopInstanceForm.value = { ...defaultLoopInstanceForm.value };
  // 取消多实例配置
  if (type === "Null") {
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {loopCharacteristics: null});
    noticeTaskPanel(false);
    return;
  }
  // 配置循环
  if (type === "StandardLoop") {
    const loopCharacteristicsObject = modelerStore.getModdle.create("bpmn:StandardLoopCharacteristics");
    modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
      loopCharacteristics: loopCharacteristicsObject
    });
    multiLoopInstance.value = null;
    return;
  }
  // 时序
  if (type === "SequentialMultiInstance") {
    multiLoopInstance.value = modelerStore.getModdle.create("bpmn:MultiInstanceLoopCharacteristics", {
      isSequential: true
    });
  } else {
    multiLoopInstance.value = modelerStore.getModdle.create("bpmn:MultiInstanceLoopCharacteristics");
  }
  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    loopCharacteristics: multiLoopInstance.value
  });

  // 默认初始化设置
  loopInstanceForm.value.collection = props.id + "_assigneeList";
  loopInstanceForm.value.elementVariable = props.id + "_assignee";
  updateLoopBase();
  updateLoopCondition();
  noticeTaskPanel(true);
}

// 循环基数
function updateLoopCardinality(cardinality) {
  let loopCardinality = null;
  if (cardinality && cardinality.length) {
    loopCardinality = modelerStore.getModdle.create("bpmn:FormalExpression", {body: cardinality});
  }
  modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, multiLoopInstance.value, {
    loopCardinality
  });
}

// 完成条件
function updateLoopCondition() {
    let completionCondition = null;
    if (loopInstanceForm.value.percentage) {
      const rate = loopInstanceForm.value.percentage / 100;
      const condition = "${multiEl.calculationCondition(execution," + rate + ")}";
      completionCondition = modelerStore.getModdle.create("bpmn:FormalExpression", {body: condition});
    }
    modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, multiLoopInstance.value, {
    completionCondition
  });
}

// 重试周期
function updateLoopTimeCycle(timeCycle) {
  const extensionElements = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: [
      modelerStore.getModdle.create(`flowable:FailedJobRetryTimeCycle`, {
        body: timeCycle
      })
    ]
  });
  modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, multiLoopInstance.value, {
    extensionElements
  });
}

// 直接更新的基础信息
function updateLoopBase() {
  modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, multiLoopInstance.value, {
    collection: loopInstanceForm.value.collection || null,
    elementVariable: loopInstanceForm.value.elementVariable || null
  });
}

// 各异步状态
function updateLoopAsync(key) {
  const {asyncBefore, asyncAfter} = loopInstanceForm.value;
  let asyncAttr = Object.create(null);
  if (!asyncBefore && !asyncAfter) {
    Reflect.set(loopInstanceForm.value, "exclusive", false);
    asyncAttr = {asyncBefore: false, asyncAfter: false, exclusive: false, extensionElements: null};
  } else {
    asyncAttr[key] = loopInstanceForm.value[key];
  }
  modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, multiLoopInstance.value, asyncAttr);
}

function handleAssignSelect(selection) {
  if (selection) {
    if (currentType.value === 'exp') {
      // 处理表达式选中数据
      selectExpData.value = JSON.parse(JSON.stringify(selection));
    } else {
      // 处理审批选中数据
      selectDataList.value = JSON.parse(JSON.stringify(selection));
    }
  }
}

// 保存选中的数据
function saveAssignSelect() {
  if (currentType.value === 'exp') {
    const selection = selectExpData.value
    if (selection.id) {
      loopInstanceForm.value.collection = selection.expression;
    }
  } else {
    // 选择人员时,固定写入表达式
    const multiUserId = assignSelect(currentType.value, "", selectDataList.value).map(item => item.id).join(',') || '';
    loopInstanceForm.value.collection = "${multiEl.setFixedAssignee(execution,'" + multiUserId + "')}";
  }
 cancelAssignSelect();
}

// 固定写法
function handleReduction() {
  loopInstanceForm.value.collection = props.id + "_assigneeList";
  updateLoopBase();
}

// 选择会签人员
function handleMultiSelect(type) {
  currentType.value = type;
  // 根据不同的 assignType，设置不同的组件
  switch (type) {
    case 'users':
      currentComponent.value = FlowUsers;
      break;
    case 'exp':
      currentComponent.value = FlowExp;
      break;
    default:
      currentComponent.value = null;
  }
  multiOpen.value = true;
}

// 取消会签人员
function cancelAssignSelect() {
  updateLoopBase();
  multiOpen.value = false;
}

// 从字符串中提取数字
function extractNumberFromString(completionCondition) {
  if (completionCondition) {
    const regex = /-?\d+(\.\d+)?/;
    const matches = completionCondition.match(regex);
    if (matches) {
      return matches[0] * 100;
    } else {
      return 100;
    }
  } else {
    return 100;
  }
}

// 通知任务面板多实例有变化
function noticeTaskPanel(value){
  EventBus.emit('multiInstance-updated', value);
}

</script>
<style lang="scss">
@import '../style/process-panel';
.custom-form-item .el-form-item__content {
  display: initial; /* 取消掉默认的属性 */
}
/* input-number属性设置 */
.custom-input-number {
  margin-left: 6px;
  width: 80px;
}
.custom-input-number .el-input__inner {
  margin-right: 30px;
}
</style>

