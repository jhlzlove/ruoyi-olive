<template>
  <div class="panel-tab__content">
    <el-form label-width="60px" status-icon @submit.native.prevent>
      <el-form-item label="抄送人">
        <el-button round plain style="margin-right: 5px;margin-bottom: 5px" type="primary" icon="Plus" size="small" class="custom-button-size" @click="userVisible = true">添加抄送</el-button>
        <el-tag
            style="margin-right: 5px;"
            v-for="user in selectCopyUserList"
            :key="user.userId"
            closable
            @close="removeCopy(user)">
          {{user.nickName}}
        </el-tag>
      </el-form-item>
    </el-form>

    <!--选择人员-->
    <el-dialog
      title="选择人员"
      v-model="userVisible"
      width="60%"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <flow-user v-if="userVisible" :selectData="copyUser" @handleAssignSelect="handleAssignSelect"></flow-user>
      <template #footer>
        <span class="dialog-footer">
        <el-button @click="userVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitSelect">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CopyPanel">
import {StrUtil} from "@/utils/StrUtil";
import FlowUser from "@/components/Flow/Design/User";
import useModelerStore from '@/store/modules/modeler'
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});
const userVisible = ref(false);
// 数据回显
const copyUser = ref(null);
const selectCopyUserList = ref([]);
const selectDataList = ref([]);
const otherExtensionList = ref([]);
const bpmnElementCopyList = ref([]);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        resetCopyList();
      }
    },
    {immediate: true}
)

// 方法区

// 用户选中数据
const handleAssignSelect = (selection) => {
  if (selection) {
      selectDataList.value = JSON.parse(JSON.stringify(selection));
  }
}

// 保存选中的数据
function submitSelect() {
 if (selectDataList.value.length > 0){
   const newUserList = [];
   selectDataList.value.forEach(user => {
     const {userId, nickName} = user;
     const newUserObject = modelerStore.getModdle.create(`flowable:Copy`, {userId, nickName});
     newUserList.push(newUserObject);
   })
   const userObject = modelerStore.getModdle.create(`flowable:CopyItem`, {
     values: newUserList
   });
   updateElementExtensions(userObject);
   resetCopyList();
 }
  userVisible.value = false;
  selectDataList.value = [];
}

// 初始化数据
function resetCopyList() {
  otherExtensionList.value = [];
  const bpmnElementCopyItem =
      modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => {
        if (ex.$type !== `flowable:CopyItem`) {
          otherExtensionList.value.push(ex);
        }
        if (ex.values) {
          return ex.$type === `flowable:CopyItem`;
        }
      }) ?? [];
  if (bpmnElementCopyItem.length > 0) {
    // 保存所有的抄送人员信息
    bpmnElementCopyList.value = bpmnElementCopyItem.reduce((pre, current) => pre.concat(current.values), []);
    // 复制 显示
    selectCopyUserList.value = JSON.parse(JSON.stringify(bpmnElementCopyList.value ?? []));
    copyUser.value = selectCopyUserList.value;
  }
}

// 移除抄送人员
function removeCopy(user) {
  proxy.$confirm("确认移除该抄送人员吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  }).then(() => {
    bpmnElementCopyList.value.splice(bpmnElementCopyList.value.find(item => item.userId === user.userId), 1);
    const propertiesObject = modelerStore.getModdle.create(`flowable:CopyItem`, {
      values: bpmnElementCopyList.value
    });
    updateElementExtensions(propertiesObject);
    resetCopyList();
  }).catch(() => console.info("操作取消"));
}

// 更新扩展抄送人员信息
function updateElementExtensions(properties) {
  const extensions = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: otherExtensionList.value.concat([properties])
  });

  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    extensionElements: extensions
  });
}

</script>
