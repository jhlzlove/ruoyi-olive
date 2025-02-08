<template>
  <div>
    <el-form-item label="定时器">
        <template #label>
          <span>定时器</span>
          <el-popover placement="left" :width="600" trigger="hover" title="定时器定义类型">
              <template #reference>
                  <el-icon style="margin-top: 8px"><QuestionFilled /></el-icon>
              </template>
              <h4>指定时间：设置指定时间触发</h4>
              <span>2022-03-23T11:11:11 (日期与时间之间使用T分割)，表示2022-03-23 11:11:11触发,也可以通过执行时传入变量，动态设置触发时间</span>

              <h4>等待时长：多久后触发</h4>
              <span>备注：P是开始的标记，所有规则必须以P开头；存在时分秒时，分隔符T不可以省略</span>
              <el-table :data="timeDurationData" border size="small">
                  <el-table-column  property="letter" label="字母"/>
                  <el-table-column width="200" property="remark" label="说明"/>
                  <el-table-column  property="example" label="示例"/>
                  <el-table-column  property="meaning" label="持续含义"/>
              </el-table>

              <h4>重复周期：循环执行</h4>
              <span>备注：R为循环定时器的开始的标记</span>
              <el-table :data="timeCycleData" border size="small">
                  <el-table-column  property="syntax" label="语法"/>
                  <el-table-column  property="example" label="示例"/>
                  <el-table-column  property="meaning" label="含义"/>
              </el-table>
          </el-popover>
        </template>
        <el-select v-model="bpmnFormData.timerType" class="m-2 select-margin" placeholder="请选择定时器定义类型" @change="handleTimerChange">
          <el-option
              v-for="item in timerList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <template v-if="bpmnFormData.timerType === 'timeDuration'">
            <!--等待时长设置-->
            <el-popover placement="top" trigger="click" title="等待时长设置" width="320px" append-to-body>
                <el-input-number controls-position="right" data-unit="年" :min="0" v-model="bpmnFormData.timeDuration.years" class="my-el-input-number" @change="changeTimeDuration" />
                <el-input-number controls-position="right" data-unit="月" :min="0" v-model="bpmnFormData.timeDuration.months" class="my-el-input-number" @change="changeTimeDuration" />
                <el-input-number controls-position="right" data-unit="日" :min="0" v-model="bpmnFormData.timeDuration.days" class="my-el-input-number" @change="changeTimeDuration"/>
                <br/>
                <el-input-number controls-position="right" data-unit="时" :min="0" v-model="bpmnFormData.timeDuration.hours" class="my-el-input-number" @change="changeTimeDuration"/>
                <el-input-number controls-position="right" data-unit="分" :min="0" v-model="bpmnFormData.timeDuration.minutes" class="my-el-input-number" @change="changeTimeDuration"/>
                <el-input-number controls-position="right" data-unit="秒" :min="0" v-model="bpmnFormData.timeDuration.seconds" class="my-el-input-number" @change="changeTimeDuration"/>

                <template #reference>
                  <el-input v-model="bpmnFormData.timerValue" placeholder="请输入等待时长">
                      <template #append>
                      <el-button icon="Setting" ></el-button>
                      </template>
                  </el-input>
                </template>
            </el-popover>
        </template>
        <template v-else-if="bpmnFormData.timerType === 'timeDate'">
            <el-date-picker v-model="bpmnFormData.timerValue" format="YYYY-MM-DDTHH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" type="datetime" placeholder="请选择执行时间" @change="updateTimerValue"/>
        </template>
        <template v-else>
            <el-input v-model="bpmnFormData.timerValue" placeholder="请输入定时周期" @change="updateTimerValue"/>
        </template>
    </el-form-item>
  </div>
</template>

<script setup name="TimerEventPanel">
import useModelerStore from '@/store/modules/modeler'
import {uuid, checkEventType} from "@/components/Process/common/bpmnUtils";
const modelerStore = useModelerStore()

const {proxy} = getCurrentInstance();
const data = reactive({
  bpmnFormData: {
    timerType: 'timeDate',
    timerValue: '',
    timeDuration: {
      years: undefined,
      months: undefined,
      days: undefined,
      hours: undefined,
      minutes: undefined,
      seconds: undefined,
    },
    eventDefinitions: []
  },
  timerList: [
    {label: '指定时间', value: 'timeDate'},
    {label: '等待时长', value: 'timeDuration'},
    {label: '重复周期', value: 'timeCycle'},
  ],
  timeDurationData: [
    //备注：P是开始的标记，所有规则必须以P开头；存在时分秒时，分隔符T不可以省略
    {letter: 'P', remark: '开始标记', example: 'P', meaning: ''},
    {letter: 'Y', remark: '年', example: 'P1Y', meaning: '1年后执行'},
    {letter: 'M', remark: '月', example: 'P1M', meaning: '1月后执行'},
    {letter: 'D', remark: '日', example: 'P1D', meaning: '1日后执行'},
    {letter: 'T', remark: '时间和日期的分割标记', example: 'P1DT1H', meaning: '隔天一小时后执行'},
    {letter: 'H', remark: '时', example: 'PT1H', meaning: '1小时后执行'},
    {letter: 'M', remark: '分', example: 'PT1M', meaning: '1分钟后执行'},
    {letter: 'S', remark: '秒', example: 'PT1S', meaning: '1秒后执行'},
  ],
  timeCycleData: [
    //备注：R为循环定时器的开始的标记
    { syntax: '循环次数次/每次间隔', example: 'R3/PT1M', meaning: '循环3次，1分钟执行一次' },
    { syntax: '循环次数次/开始循环时间/每次间隔', example: 'R3/2022-04-01T11:11:11/PT1M', meaning: '循环3次/2022-04-01T11:11:11开始循环/1分钟执行一次' },
    { syntax: '无限循环/每次间隔/结束时间', example: 'R/PT1M/2022-04-01', meaning: '无限循环，1分钟执行一次，2022-04-01日结束' },
    { syntax: '设置变量循环', example: 'R/PT1M/{变量名}', meaning: '无限循环，1分钟执行一次，结束时间为变量' },
  ],
});

const { bpmnFormData, timerList, timeDurationData,timeCycleData } = toRefs(data);

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

function resetTaskForm() {
  bpmnFormData.value = {
    timerType: 'timeDate',
    timerValue: '',
    timeDuration: {
      years: undefined,
      months: undefined,
      days: undefined,
      hours: undefined,
      minutes: undefined,
      seconds: undefined,
    },
    eventDefinitions:[]
  };
  bpmnFormData.value.$type = modelerStore.getBpmnElement?.businessObject?.$type;
  // 处理事件
  if (checkEventType(bpmnFormData.value.$type)) {
    getFlowEvent(bpmnFormData.value, modelerStore.getBpmnElement.businessObject)
  }
}

// 处理事件
function getFlowEvent(bpmnFormData, businessObject) {
  if (businessObject.eventDefinitions && businessObject.eventDefinitions.length > 0) {
    if (businessObject.eventDefinitions[0].$type.indexOf("Timer") !== -1) {
      for (let key in businessObject.eventDefinitions[0]) {
        if (key.indexOf("time") !== -1) {
          bpmnFormData.timerType = key;
          bpmnFormData.timerValue = businessObject.eventDefinitions[0][key].body;
          if (key === 'timeDuration' && bpmnFormData.timerValue) {
            bpmnFormData.timeDuration = parseISO8601(bpmnFormData.timerValue);
          }
          break;
        }
      }
    }
  }
}

// 切换定时类型
function handleTimerChange(val) {
  bpmnFormData.value.timerType = val;
  bpmnFormData.value.timerValue = '';
  updateTimerValue(bpmnFormData.value.timerValue);
}

// 更新定时事件
function updateTimerValue(timerValue) {
  const timeDefinition = modelerStore.getModdle.create("bpmn:FormalExpression", {
    body: timerValue
  });
  const TimerEventDefinition = modelerStore.getModdle.create("bpmn:TimerEventDefinition", {
    id: `TimerEventDefinition_${uuid(8)}`,
    [bpmnFormData.value.timerType]: timeDefinition
  });
  modelerStore.getBpmnElement.businessObject.eventDefinitions = [TimerEventDefinition];
}

// 设置TimeDuration时间类型
function changeTimeDuration() {
  bpmnFormData.value.timerValue = convertToISO8601(bpmnFormData.value.timeDuration);
  updateTimerValue(bpmnFormData.value.timerValue);
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
</script>

<style>
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

.select-margin {
  margin-bottom: 10px;
}
</style>
