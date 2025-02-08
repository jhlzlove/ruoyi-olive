<template>
  <div class="panel-tab__content">
    <el-table :data="elementPropertyList" size="small" max-height="240" border fit>
      <el-table-column label="序号" width="60px" type="index" />
      <el-table-column label="属性名" prop="name" width="80px" show-overflow-tooltip />
      <el-table-column label="属性值" prop="value" width="80px" show-overflow-tooltip />
      <el-table-column label="操作" min-width="100px">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="openAttributesForm(scope.row, scope.$index)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button size="small" link type="danger" @click="removeAttributes(scope.row, scope.$index)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="element-drawer__button_save">
      <el-button type="primary" size="small" class="custom-button-size" icon="Plus" @click="openAttributesForm(null, -1)">添加属性</el-button>
    </div>

    <el-dialog v-model="propertyFormModelVisible" title="属性配置" width="600px" append-to-body destroy-on-close>
      <el-form :model="propertyForm" label-width="80px" ref="attributeFormRef" @submit.native.prevent>
        <el-form-item label="属性名：" prop="name">
          <el-input v-model="propertyForm.name" clearable />
        </el-form-item>
        <el-form-item label="属性值：" prop="value">
          <el-input v-model="propertyForm.value" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
            <el-button size="small" class="custom-button-size" @click="propertyFormModelVisible = false">取 消</el-button>
            <el-button size="small" class="custom-button-size" type="primary" @click="saveAttribute">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PropertyPanel">
import useModelerStore from '@/store/modules/modeler'
import {StrUtil} from "@/utils/StrUtil";
const modelerStore = useModelerStore()
const { proxy } = getCurrentInstance();

/** 组件传值  */
const props = defineProps({
  id: {
    type: String,
    required: true
  },
});
const elementPropertyList =  ref([]);
const otherExtensionList =  ref([]);
const bpmnElementPropertyList =  ref([]);
const editingPropertyIndex=  ref(-1);
const propertyFormModelVisible=  ref(false);

const data = reactive({
  propertyForm: {},
});

const { propertyForm } = toRefs(data);

/** 传值监听 */
watch(() => props.id, newVal => {
      if (newVal) {
        if (StrUtil.isNotBlank(newVal)) {
          resetAttributesList();
        }
      }
    },
    {immediate: true}
)

// 方法区
function resetAttributesList() {
  otherExtensionList.value = []; // 其他扩展配置
  const bpmnElementProperties =
      modelerStore.getBpmnElement.businessObject?.extensionElements?.values?.filter(ex => {
        if (ex.$type !== `flowable:Properties`) {
          otherExtensionList.value.push(ex);
        }
        return ex.$type === `flowable:Properties`;
      }) ?? [];

  // 保存所有的 扩展属性字段
  bpmnElementPropertyList.value = bpmnElementProperties.reduce((pre, current) => pre.concat(current.values), []);
  // 复制 显示
  elementPropertyList.value = JSON.parse(JSON.stringify(bpmnElementPropertyList.value ?? []));
}

function openAttributesForm(attr, index) {
  editingPropertyIndex.value = index;
  propertyForm.value = index === -1 ? {} : JSON.parse(JSON.stringify(attr));
  propertyFormModelVisible.value = true;
  proxy.$nextTick(() => {
    if (proxy.$refs["attributeFormRef"]) proxy.$refs["attributeFormRef"].clearValidate();
  });
}

function removeAttributes(attr, index) {
  proxy.$confirm("确认移除该属性吗？", "提示", {
    confirmButtonText: "确 认",
    cancelButtonText: "取 消"
  })
      .then(() => {
        elementPropertyList.value.splice(index, 1);
        bpmnElementPropertyList.value.splice(index, 1);
        // 新建一个属性字段的保存列表
        const propertiesObject = modelerStore.getModdle.create(`flowable:Properties`, {
          values: bpmnElementPropertyList.value
        });
        updateElementExtensions(propertiesObject);
        resetAttributesList();
      })
      .catch(() => console.info("操作取消"));
}

function saveAttribute() {
  const {name, value} = propertyForm.value;
  if (editingPropertyIndex.value !== -1) {
    modelerStore.getModeling.updateModdleProperties(modelerStore.getBpmnElement, bpmnElementPropertyList.value[editingPropertyIndex.value], {
      name,
      value
    });
  } else {
    // 新建属性字段
    const newPropertyObject = modelerStore.getModdle.create(`flowable:Property`, {name, value});
    // 新建一个属性字段的保存列表
    const propertiesObject = modelerStore.getModdle.create(`flowable:Properties`, {
      values: bpmnElementPropertyList.value.concat([newPropertyObject])
    });
    updateElementExtensions(propertiesObject);
  }
  propertyFormModelVisible.value = false;
  resetAttributesList();
}

function updateElementExtensions(properties) {
  const extensions = modelerStore.getModdle.create("bpmn:ExtensionElements", {
    values: otherExtensionList.value.concat([properties])
  });

  modelerStore.getModeling.updateProperties(modelerStore.getBpmnElement, {
    extensionElements: extensions
  });
}
</script>
