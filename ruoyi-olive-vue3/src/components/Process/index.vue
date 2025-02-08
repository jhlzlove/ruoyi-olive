<template>
  <div v-loading="isView" class="flow-containers" :class="{ 'view-mode': isView }">
    <el-container style="height: 100%">
      <el-header style="border-bottom: 1px solid rgb(218 218 218);height: auto;padding-left:0">
        <div class="bpmn-toolbar">
          <el-button-group>
            <el-tooltip effect="dark" content="打开文件" placement="bottom">
              <el-button icon="FolderOpened" size="small" class="custom-button-size" @click="openBpmn">
                <input type="file" ref="importRef" style="display: none" accept=".xml,.bpmn" @change="changeImportFile" />
              </el-button>
            </el-tooltip>
            <el-popover placement="bottom" trigger="hover">
              <template #reference>
                <el-button class="m-2 custom-button-size" size="small" icon="View"></el-button>
              </template>
              <div style="display: grid;grid-template-columns: 1fr;grid-row-gap: 8px;padding: 8px 0;">
                <el-button size="small" class="custom-button-size" style="margin-left: 12px" @click="showXML">预览 XML</el-button>
                <el-button size="small" class="custom-button-size" @click="showJSON">预览 JSON</el-button>
              </div>
            </el-popover>
            <el-popover placement="bottom" trigger="hover">
              <template #reference>
                <el-button class="m-2 custom-button-size" size="small" icon="Download"></el-button>
              </template>
              <div style="display: grid;grid-template-columns: 1fr;grid-row-gap: 8px;padding: 8px 0;">
                <el-button size="small" class="custom-button-size" style="margin-left: 12px" @click="downloadProcessAsBpmn">导出为 Bpmn</el-button>
                <el-button size="small" class="custom-button-size" @click="downloadProcessAsXml">导出为 XML</el-button>
                <el-button size="small" class="custom-button-size" @click="downloadProcessAsSvg">导出为 SVG</el-button>
              </div>
            </el-popover>
          </el-button-group>

          <el-button-group>
            <el-tooltip effect="dark" content="自适应屏幕" placement="bottom">
              <el-button icon="Rank" size="small" class="custom-button-size" @click="fitViewport" />
            </el-tooltip>
            <el-tooltip effect="dark" content="放大" placement="bottom">
              <el-button icon="ZoomIn" size="small" class="custom-button-size" @click="zoomViewport(true)" />
            </el-tooltip>
            <el-tooltip effect="dark" content="缩小" placement="bottom">
              <el-button icon="ZoomOut" size="small" class="custom-button-size" @click="zoomViewport(false)" />
            </el-tooltip>
            <el-tooltip effect="dark" content="后退" placement="bottom">
              <el-button icon="Back" size="small" class="custom-button-size" @click="bpmnModel.get('commandStack').undo()" />
            </el-tooltip>
            <el-tooltip effect="dark" content="前进" placement="bottom">
              <el-button icon="Right" size="small" class="custom-button-size" @click="bpmnModel.get('commandStack').redo()" />
            </el-tooltip>
            <el-tooltip effect="dark" content="保存为草稿" placement="bottom">
              <el-button icon="DocumentAdd" size="small" class="custom-button-size" @click="saveDraft" />
            </el-tooltip>
          </el-button-group>

          <el-button-group style="margin-left: auto;">
            <el-button type="warning" size="small" class="custom-button-size" @click="newDiagram">重新绘制</el-button>
            <el-button type="primary" size="small" class="custom-button-size" @click="save">保存模型</el-button>
            <el-popconfirm title="确定退出流程设计页面吗？" :width="250" @confirm="goBack">
              <template #reference>
                <el-button type="danger" size="small" class="custom-button-size">关闭</el-button>
              </template>
            </el-popconfirm>
          </el-button-group>
        </div>
      </el-header>
      <!-- 流程设计页面 -->
      <el-container style="align-items: stretch">
        <el-main>
          <div ref="canvas" class="canvas" />
          <div class="button-group">
            <el-tooltip class="item" effect="dark" placement="left">
              <template #content>{{designer ? '收起' : '展开'}}</template>
              <el-button circle :icon="designer ? 'DArrowRight':'DArrowLeft'" @click="togglePanel"/>
            </el-tooltip>
            <el-tooltip class="item" effect="dark" placement="left">
              <template #content>{{ simulationStatus ? '退出模拟' : '开启模拟' }}</template>
              <el-button circle icon="Share" @click="processSimulation"/>
            </el-tooltip>
            <el-tooltip class="item" effect="dark" placement="left">
              <template #content>{{ bpmnlintStatus ? '关闭检查' : '开启检查' }}</template>
              <el-button circle icon="FirstAidKit" @click="handlerIntegrityCheck"/>
            </el-tooltip>
          </div>
        </el-main>

        <!--右侧属性栏-->
        <el-card shadow="never" :class="{'normalPanel':designer,'miniPanel':!designer}">
          <designer v-if="loadCanvas"></designer>
        </el-card>
      </el-container>
    </el-container>
  </div>
</template>

<script setup name="BpmnModel">
// 汉化
import { addDraft, updateDraft } from "@/api/flowable/draft"
import { StrUtil } from '@/utils/StrUtil'
import Modeler from 'bpmn-js/lib/Modeler'
import customTranslate from './customPanel/customTranslate'
import Designer from './designer.vue'
import getInitStr from './flowable/init'
// 引入flowable的节点文件
import useAppStore from '@/store/modules/app'
import useModelerStore from '@/store/modules/modeler'
import lintModule from 'bpmn-js-bpmnlint'
import BpmnColorPickerModule from 'bpmn-js-color-picker'
import { CreateAppendAnythingModule } from 'bpmn-js-create-append-anything'
import TokenSimulationModule from 'bpmn-js-token-simulation'
import GridModule from 'diagram-js-grid'
import bpmnLintConfig from './common/bpmnlintconfig'
import customControlsModule from './customPanel'
import FlowableModule from './flowable/flowable.json'
const modelerStore = useModelerStore();
const appStore = useAppStore()

import { markRaw } from "vue"

const route = useRoute();
const { proxy } = getCurrentInstance();

const bpmnModel = ref(null);
const zoom = ref(1);
const simulationStatus = ref(false);
const bpmnlintStatus = ref(false);
const simulation = ref(true);
const designer = ref(true);

// 当前组件渲染然后再加载canvas
const loadCanvas = ref(false);

/** 组件传值  */
const props = defineProps({
  xml: {
    type: String,
    default: ''
  },
  isView: {
    type: Boolean,
    default: false
  },
  category: {
    type: [String , Number],
    default: null
  },
  draftId: {
    type: [String , Number],
    default: null
  },
});

const emit = defineEmits(['showXML','showJSON','save'])

/** 传值监听 */
watch(() => props.xml, newVal => {
      toggleSideBar();
      if (StrUtil.isNotBlank(newVal)) {
        createNewDiagram(newVal)
      } else {
        newDiagram()
      }
    }, {immediate: true}
)

/** 创建bpmn 实例 */
onMounted(() => {
    const modeler = new Modeler({
        container: proxy.$refs.canvas,
        additionalModules: [
            customControlsModule,
            TokenSimulationModule,
            BpmnColorPickerModule,
            CreateAppendAnythingModule,
            lintModule,
            GridModule,
            { //汉化
                translate: ['value', customTranslate]
            },
        ],
        linting: {
            bpmnlint: bpmnLintConfig
        },
        moddleExtensions: {
            flowable: FlowableModule
        }
    })
    bpmnModel.value = markRaw(modeler);
    // 注册 modeler 相关信息
    modelerStore.setModeler(markRaw(modeler));
    modelerStore.setModules('moddle', markRaw(modeler.get('moddle')));
    modelerStore.setModules('modeling', markRaw(modeler.get('modeling')));
    modelerStore.setModules('eventBus', markRaw(modeler.get('eventBus')));
    modelerStore.setModules('bpmnFactory', markRaw(modeler.get('bpmnFactory')));
    modelerStore.setModules('elementFactory', markRaw(modeler.get('elementFactory')));
    modelerStore.setModules('elementRegistry', markRaw(modeler.get('elementRegistry')));
    modelerStore.setModules('selection', markRaw(modeler.get('selection')));
    modelerStore.setModules('canvas', markRaw(modeler.get('canvas')));
    // 存入当前流程分类
    if (props.category) {
        modelerStore.setModules('category', props.category)
    }
    // 直接点击新建按钮时,进行新增流程图
    if (StrUtil.isBlank(props.xml)) {
        newDiagram()
    } else {
        createNewDiagram(props.xml)
    }
    // 创建modeler事件
    proxy.$nextTick(() => {
      createModelerEvent(modeler);
    })
})

// 根据默认文件初始化流程图
function newDiagram() {
  createNewDiagram(getInitStr())
}

// 根据提供的xml创建流程图
async function createNewDiagram(data) {
  // 将字符串转换成图显示出来
  // data = data.replace(/<!\[CDATA\[(.+?)]]>/g, '&lt;![CDATA[$1]]&gt;')
  if (StrUtil.isNotBlank(modelerStore.getModeler)) {
    data = data.replace(/<!\[CDATA\[(.+?)]]>/g, function (match, str) {
      return str.replace(/</g, '&lt;')
    })
    try {
      await modelerStore.getModeler.importXML(data)
      fitViewport()
    } catch (err) {
      console.error(err.message, err.warnings)
    }
  }
}

// 注册设计器点击事件
function createModelerEvent(modeler){
  // 流程校验
  modeler.on('linting.toggle', function(event) {
    bpmnlintStatus.value = event.active;
  });
}

// 让图能自适应屏幕
function fitViewport() {
  zoom.value = modelerStore.getCanvas.zoom('fit-viewport')
  const bbox = document.querySelector('.flow-containers .viewport').getBBox()
  const currentViewBox = modelerStore.getCanvas.viewbox()
  const elementMid = {
    x: bbox.x + bbox.width / 2 - 65,
    y: bbox.y + bbox.height / 2
  }
  modelerStore.getCanvas.viewbox({
    x: elementMid.x - currentViewBox.width / 2,
    y: elementMid.y - currentViewBox.height / 2,
    width: currentViewBox.width,
    height: currentViewBox.height
  })
  zoom.value = bbox.width / currentViewBox.width * 1.8
  loadCanvas.value = true;
}

// 放大缩小
function zoomViewport(zoomIn = true) {
  zoom.value = modelerStore.getCanvas.zoom()
  zoom.value += (zoomIn ? 0.1 : -0.1)
  modelerStore.getCanvas.zoom(zoom.value)
}

// 获取流程基础信息
function getProcess() {
  const element = getProcessElement()
  return {
    id: element.id,
    name: element.name,
    category: element.processCategory
  }
}

// 获取流程主面板节点
function getProcessElement() {
  const rootElements = modelerStore.getModeler.getDefinitions().rootElements
  for (let i = 0; i < rootElements.length; i++) {
    if (rootElements[i].$type === 'bpmn:Process') return rootElements[i]
  }
}

// 保存xml
async function saveXML(download = false) {
  try {
    const {xml} = await modelerStore.getModeler.saveXML({format: true})
    if (download) {
      downloadFile(`${getProcessElement().name}.bpmn20.xml`, xml, 'application/xml')
    }
    return xml
  } catch (err) {
    console.log(err)
  }
}

// 在线查看xml
async function showXML() {
  try {
    const xmlStr = await saveXML()
    emit('showXML', xmlStr)
  } catch (err) {
    console.log(err)
  }
}

// 在线查看json
async function showJSON() {
  try {
    const xmlStr = await saveXML();
    const xml = await modelerStore.getModdle.fromXML(xmlStr);
    const jsonStr = JSON.stringify(xml, null, 2);
    emit('showJSON', jsonStr);
  } catch (err) {
    console.error(err);
  }
}

// 保存流程图为svg
async function saveImg(type = 'svg', download = false) {
  try {
    const {svg} = await modelerStore.getModeler.saveSVG({format: true})
    if (download) {
      downloadFile(getProcessElement().name, svg, 'image/svg+xml')
    }
    return svg
  } catch (err) {
    console.log(err)
  }
}

// 保存流程图
async function save() {
  const process = getProcess()
  const xml = await saveXML()
  const svg = await saveImg()
  const result = {process, xml, svg}
  emit('save', result)
  window.parent.postMessage(result, '*')
}

// 打开流程文件
function openBpmn() {
  proxy.$refs.importRef && proxy.$refs.importRef.click();
}

function changeImportFile() {
  try {
    if (proxy.$refs.importRef && proxy.$refs.importRef.files) {
      const file = proxy.$refs.importRef.files[0];
      const reader = new FileReader();
      reader.readAsText(file);
      reader.onload = function () {
        const xmlStr = this.result;
        modelerStore.getModeler.importXML(xmlStr);
      };
      proxy.$refs.importRef.value = null;
      proxy.$refs.importRef.files = null;
    }
  } catch (e) {
    console.error(e);
  }
}

async function downloadProcess(type) {
  const name = getProcessElement().name;
  try {
    const modeler = modelerStore.getModeler;
    // 按需要类型创建文件并下载
    if (type === "xml" || type === "bpmn") {
      const {err, xml} = await modeler.saveXML();
      // 读取异常时抛出异常
      if (err) {
        console.error(`[Process Designer Warn ]: ${err.message || err}`);
      }
      const {href, filename} = setEncoded(type.toUpperCase(), name, xml);
      downloadFile(href, filename);
    } else {
      const {err, svg} = await modeler.saveSVG();
      // 读取异常时抛出异常
      if (err) {
        return console.error(err);
      }
      const {href, filename} = setEncoded("SVG", name, svg);
      downloadFile(href, filename);
    }
  } catch (e) {
    console.error(`[Process Designer Warn ]: ${e.message || e}`);
  }
}
function downloadProcessAsBpmn() {
  downloadProcess("bpmn");
}
function downloadProcessAsXml() {
  downloadProcess("xml");
}
function downloadProcessAsSvg() {
 downloadProcess("svg");
}

// 根据所需类型进行转码并返回下载地址
function setEncoded(type, filename, data) {
  const encodedData = encodeURIComponent(data);
  return {
    filename: `${filename}.${type.toLowerCase()}`,
    href: `data:application/${type === "svg" ? "text/xml" : "bpmn20-xml"};charset=UTF-8,${encodedData}`,
    data: data
  };
}

// 文件下载方法
function downloadFile(href, filename) {
  if (href && filename) {
    const a = document.createElement("a");
    a.download = filename; //指定下载的文件名
    a.href = href; //  URL对象
    a.click(); // 模拟点击
    URL.revokeObjectURL(a.href); // 释放URL 对象
  }
}

/** 隐藏属性面板*/
function togglePanel() {
  designer.value = !designer.value;
}

/** 流程模拟器*/
function processSimulation() {
  simulationStatus.value = !simulationStatus.value;
  simulation.value && modelerStore.getModeler.get("toggleMode").toggleMode();
}

/** 流程校验*/
function handlerIntegrityCheck() {
  const linting = modelerStore.getModeler.get('linting')
  linting && linting.toggle();
  // bpmnlintStatus.value = !bpmnlintStatus.value;
}


// 保存草稿
async function saveDraft() {
  const process = getProcess();
  const xml = await saveXML();
  const params = {
    id: props.draftId,
    name: process.name,
    category: process.category,
    xml: xml,
    formId: modelerStore.getFormId,
  };
  try {
    const res = params.id != null ? await updateDraft(params) : await addDraft(params);
    if (res.code === 200) {
      proxy.$modal.msgSuccess("保存草稿成功");
      goBack();
    } else {
      proxy.$modal.msgError("保存草稿成功");
    }
  } catch (error) {
    proxy.$modal.msgError("保存草稿失败");
  }
}

/** 关闭当前标签页并返回上个页面 */
function goBack() {
  const obj = {path: "/flowable/definition" , query: { t: Date.now()}};
  proxy.$tab.closeOpenPage(obj);
  toggleSideBar();
}
// 隐藏或显示侧边栏
function toggleSideBar() {
  appStore.toggleSideBar();
}
</script>

<style lang="scss">
/*左边工具栏以及编辑节点的样式*/
@import "bpmn-js/dist/assets/bpmn-js.css";
@import "bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css";
@import 'bpmn-js/dist/assets/diagram-js.css'; // 基础样式
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'; // 节点基础图标
@import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'; // 节点完整图标

/*流程模拟器样式*/
@import "bpmn-js-token-simulation/assets/css/bpmn-js-token-simulation.css";
/*流程校验样式*/
@import 'bpmn-js-bpmnlint/dist/assets/css/bpmn-js-bpmnlint.css';

.view-mode {
  .el-header, .el-aside, .djs-palette, .bjs-powered-by {
    display: none;
  }
  .el-loading-mask {
    background-color: initial;
  }
  .el-loading-spinner {
    display: none;
  }
}

.flow-containers {
  width: 100%;
  height: calc(100vh - 80px);
  .canvas {
    width: 100%;
    height: 100%;
    //background: url("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImEiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHBhdGggZD0iTTAgMTBoNDBNMTAgMHY0ME0wIDIwaDQwTTIwIDB2NDBNMCAzMGg0ME0zMCAwdjQwIiBmaWxsPSJub25lIiBzdHJva2U9IiNlMGUwZTAiIG9wYWNpdHk9Ii4yIi8+PHBhdGggZD0iTTQwIDBIMHY0MCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjZTBlMGUwIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+")
  }
  .panel {
    position: absolute;
    right: 0;
    top: 50px;
    width: 300px;
  }
  .load {
    margin-right: 10px;
  }
  .normalPanel {
    width: 400px;
    height: 100%;
  }
  .miniPanel {
    width: 0;
    height: 0;
  }
  // 不显示自带流程模拟器开关
  .bts-toggle-mode {
    display: none;
  }

  .el-main {
    position: relative;
    padding: 0;
  }

  .el-main .button-group {
    display: flex;
    flex-direction: column;
    position: absolute;
    width: auto;
    height: auto;
    top: 10px;
    right: 10px;
  }

  .button-group .el-button {
    width: 100%;
    height: 100%;
    margin: 0 0 5px;
  }

  .button-group .el-button .el-icon {
    margin-right: 0;
  }


  .bpmn-toolbar {
    width: 100%;
    height: min-content;
    box-sizing: border-box;
    padding: 8px 16px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    > div[class^='el-button-group'] + div[class^='el-button-group'] {
      margin-left: 16px;
    }

    .el-button__no-padding {
      padding: 0 15px;
      height: 32px;
    }
  }
}
</style>
