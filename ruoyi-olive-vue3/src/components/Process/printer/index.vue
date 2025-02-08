<template>
<div>
    <el-container style="min-height:700px;height: 100%;">
    <el-header style="border-bottom: 1px solid rgb(218 218 218);height: auto;padding-left:0">
      <div style="display: flex; margin-top: 10px;margin-bottom: 10px; justify-content: space-between;">
        <div>
          <el-checkbox v-model="formShow">{{printData.procDefName}}</el-checkbox>
          <el-checkbox v-model="recordShow">流转过程</el-checkbox>
        </div>
        <el-button icon="Printer" type="info" v-print="printObj">打印</el-button>
      </div>
    </el-header>
    <el-scrollbar max-height="600px">
      <el-main style="margin-top:20px; padding: 0" id="printMe">
        <el-card class="box-card" shadow="never" >
          <h3 v-show="formShow" style="text-align: center">{{printData.procDefName}}</h3>

          <!--  表单数据 -->
          <div v-show="formShow" style="margin-left: 20%"  v-for="(form, index) in flowForms" :key="index">
            <v-form-render v-if="form.formType === 'online'" ref="vFormRef"/>
            <component v-else :is="form.formContent" ref="vFormRef"/>
          </div>

          <h3 v-show="recordShow" style="text-align: center">流转过程</h3>
          <!-- 审批流程 -->
          <flow-record v-if="recordShow" :flowRecordList="flowRecordList" />
        </el-card>
      </el-main>
    </el-scrollbar>
  </el-container>
</div>
</template>

<script setup name="Printer">
const { proxy } = getCurrentInstance();
import {getRecordList} from "@/api/flowable/finished";
import FlowRecord from "@/components/Process/record";

/** 组件传值  */
const props = defineProps({
  printData: {
    type: Object,
    default: null,
    required: false
  },
});
const flowRecordList = ref([]);
// 表单json
const formJson = ref({});
// 表单渲染器
const vFormRef = ref(null);
const formShow = ref(false);
const recordShow = ref(false);
// 表单内容
const flowForms = ref([]);

const data = reactive({
  // 打印
  printObj: {
    id: 'printMe',//id
    popTitle: "",//自定义设置标题
  },
});

const { printObj } = toRefs(data);

/** 传值监听 */
watch(() => props.printData, newValue => {
      if (newValue) {
        if (newValue.procDefName) {
          loadFormData(newValue);
          formShow.value = true;
        }
        if (newValue.procInsId){
          getFlowRecordList(newValue.procInsId);
        }
      }
    }, {immediate: true}
);

/** 选用表单数据 */
function loadFormData(printData) {
  flowForms.value = printData.flowForms;
  proxy.$nextTick(() => {
    flowForms.value.forEach((form, index) => {
      // 回显在线表单
      if (form.formType === "online") {
        vFormRef.value[index].setFormJson(form.formContent);
        proxy.$nextTick(() => {
          vFormRef.value[index].setFormData(printData.formData);
            proxy.$nextTick(() => {
              vFormRef.value[index].disableForm();
            })
        })
      } else {
        // 回显路由表单
        proxy.$nextTick(() => {
          vFormRef.value[index].setFormData(printData.formData, true);
        })
      }
    })
  })
}


/** 获取流程流转记录 */
function getFlowRecordList(procInsId) {
  // 调用流程记录接口
  getRecordList({procInsId: procInsId}).then(res => {
    flowRecordList.value = res.data.flowList;
    recordShow.value = true;
  })
}
</script>

<style scoped>

</style>
