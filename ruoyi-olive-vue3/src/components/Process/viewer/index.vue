<template>
  <div class="containers">
    <el-container style="align-items: stretch">
      <el-main class="flow-viewer">
        <div class="process-status">
          <span class="intro">状态：</span>
          <div class="finish">已办理</div>
          <div class="processing">处理中</div>
          <div class="todo">未进行</div>
        </div>
        <!-- 流程图显示 -->
        <div v-loading="loading" class="canvas" ref="flowCanvas"></div>
        <!--  按钮区域  -->
        <el-button-group class="button-group">
          <el-tooltip effect="dark" content="适中" placement="bottom">
            <el-button icon="Rank" @click="fitViewport" />
          </el-tooltip>
          <el-tooltip effect="dark" content="放大" placement="bottom">
            <el-button icon="ZoomIn" @click="zoomViewport(true)" />
          </el-tooltip>
          <el-tooltip effect="dark" content="缩小" placement="bottom">
            <el-button icon="ZoomOut" @click="zoomViewport(false)" />
          </el-tooltip>
      </el-button-group>
      </el-main>
    </el-container>
      <!-- 空数据 -->
<!--      <el-empty description="暂无流程数据" />-->
      <!-- 节点信息 -->
<!--      <el-descriptions v-if="flowDetail.taskName" class="flowMsgPopover margin-top" :title="flowDetail.taskName" :column="1" size="small" border>-->
<!--        <el-descriptions-item v-if="flowDetail.assigneeName" >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><User /></el-icon>-->
<!--              办理人-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.assigneeName}}-->
<!--          <el-tag type="info" size="small">{{flowDetail.deptName}}</el-tag>-->
<!--        </el-descriptions-item>-->
<!--        <el-descriptions-item v-if="flowDetail.candidate" >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><User /></el-icon>-->
<!--              候选办理-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.candidate}}-->
<!--        </el-descriptions-item>-->
<!--        <el-descriptions-item >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><Calendar /></el-icon>-->
<!--              接收时间-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.createTime}}-->
<!--        </el-descriptions-item>-->
<!--        <el-descriptions-item v-if="flowDetail.finishTime" >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><Calendar /></el-icon>-->
<!--              处理时间-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.finishTime}}-->
<!--        </el-descriptions-item>-->
<!--        <el-descriptions-item v-if="flowDetail.duration"  >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><Timer /></el-icon>-->
<!--              耗时-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.duration}}-->
<!--        </el-descriptions-item>-->
<!--        <el-descriptions-item v-if="flowDetail.comment" >-->
<!--          <template #label>-->
<!--            <div class="cell-item">-->
<!--              <el-icon :style="iconStyle"><ChatDotSquare /></el-icon>-->
<!--              处理意见-->
<!--            </div>-->
<!--          </template>-->
<!--          {{flowDetail.comment.comment}}-->
<!--        </el-descriptions-item>-->
<!--    </el-descriptions>-->
  </div>
</template>

<script setup name="BpmnViewer">
import {flowTaskInfo} from "@/api/flowable/process";
import ZoomScrollModule from "diagram-js/lib/navigation/zoomscroll";
import MoveCanvasModule from "diagram-js/lib/navigation/movecanvas";
import GridModule from 'diagram-js-grid';
import BpmnViewer from "bpmn-js/lib/Viewer";

const { proxy } = getCurrentInstance();

const bpmnViewer = ref(null);
const loading = ref(true);

/** 组件传值  */
const props = defineProps({
  // 回显数据传值
  flowData: {
    type: Object,
    default: () => {},
    required: false
  },
  procInsId: {
    type: String,
    default: ''
  },
});

// 传值监听
watch(() => props.flowData, newValue => {
      if (Object.keys(newValue).length > 0) {
        // 生成实例
        bpmnViewer.value && bpmnViewer.value.destroy();
        bpmnViewer.value = new BpmnViewer({
          container: proxy.$refs.flowCanvas,
          height: 'calc(100vh - 200px)',
          additionalModules:[
            ZoomScrollModule,
            MoveCanvasModule,
            GridModule
          ],
        });
        loadFlowCanvas(newValue);
        // 为节点注册鼠标点击事件
        // if (props.procInsId) {
        //   const eventBus = bpmnViewer.value.get('eventBus');
        //   const overlays = bpmnViewer.value.get('overlays');
        //   eventBus.on('element.click', (e) => {
        //     if (e.element.type === "bpmn:UserTask") {
        //       flowTaskInfo({procInsId: props.procInsId, elementId: e.element.id}).then(res => {
        //         flowDetail.value = res.data;
        //         setTimeout(() => {
        //           if (e.element.type === "bpmn:UserTask") {
        //             if (flowDetail.value) {
        //               genNodeDetailBox(e, overlays);
        //             }
        //           }
        //         }, 10)
        //       })
        //     }
        //   });
        //   eventBus.on('element.out', (e) => {
        //     overlays.clear();
        //   });
        // }
      }
    }
);

// 方法区
//悬浮框设置
function genNodeDetailBox(e, overlays) {
  let tempDiv = document.createElement("div");
  let popoverEl = document.querySelector('.flowMsgPopover');
  tempDiv.innerHTML= popoverEl.innerHTML;
  tempDiv.style.width = '360px';
  tempDiv.className = 'tipBox';
  tempDiv.style.background = 'rgba(255,255,255,0.95)';
  overlays.add(e.element.id, {
    position: {top: e.element.height, left: 0},
    html: tempDiv
  });
}

// 加载流程图片
async function loadFlowCanvas(flowData) {
  try {
      await bpmnViewer.value.importXML(flowData.xmlData);
      await fitViewport();
    // 流程线高亮设置
    if (flowData.nodeData !== undefined && flowData.nodeData.length > 0 && props.procInsId) {
      await fillColor(flowData.nodeData);
    }
  } catch (err) {
    console.error(err.message, err.warnings)
  }
}

// 让图能自适应屏幕
function fitViewport() {
  proxy.zoom = bpmnViewer.value.get('canvas').zoom("fit-viewport", "auto");
  loading.value = false;
}

// 放大缩小
function zoomViewport(zoomIn = true) {
  proxy.zoom = bpmnViewer.value.get('canvas').zoom()
  proxy.zoom += (zoomIn ? 0.1 : -0.1)
  if (proxy.zoom >= 0.2) bpmnViewer.value.get('canvas').zoom(proxy.zoom);
}


// 设置高亮颜色
function fillColor(nodeData) {
  const canvas = bpmnViewer.value.get('canvas')
  const definitions = bpmnViewer.value.getDefinitions();

  // 遍历流程定义的根元素中的每个节点
  definitions.rootElements[0].flowElements.forEach(node => {
    const completeTask = nodeData.find(task => task.key === node.id);
    const todoTask = nodeData.find(task => !task.completed);

    // 判断节点类型
    const isTaskOrGateway = node.$type.indexOf('Task') !== -1 || node.$type.indexOf('Gateway') !== -1;
    const isStartEvent = node.$type === 'bpmn:StartEvent';
    const isEndEvent = node.$type === 'bpmn:EndEvent';
    const isEventNode = node.$type === 'bpmn:IntermediateCatchEvent' || node.$type === 'bpmn:IntermediateThrowEvent' || node.$type === 'bpmn:BoundaryEvent';

    if (isTaskOrGateway) {
      // 用户任务节点或网关节点处理
      handleTaskOrGateway(node, canvas, nodeData, completeTask, todoTask);
    } else if (isStartEvent) {
      // 开始事件节点处理
      handleStartEvent(node, canvas, nodeData);
    } else if (isEndEvent) {
      // 结束事件节点处理
      handleEndEvent(node, canvas, nodeData);
    } else if (isEventNode) {
      // 事件节点处理
      handleEventNode(node, canvas, nodeData, completeTask);
    } else if (node.$type === 'bpmn:SubProcess') {
      if (completeTask) {
        if (todoTask && completeTask.key === todoTask.key && !todoTask.completed) {
          canvas.addMarker(node.id, todoTask.completed ? 'highlight' : 'highlight-todo');
        } else {
          canvas.addMarker(node.id, completeTask.completed ? 'highlight' : 'highlight-todo');
        }
      }
      // 子任务处理
      handleSubProcess(node, canvas, nodeData);

      // 子任务的出线处理
      node.outgoing?.forEach(flow => {
        const targetTask = nodeData.find(task => task.key === flow.targetRef.id);
        if (targetTask) {
          if (flow.$type === 'bpmn:SequenceFlow') {
            // 顺序流程线处理
            const sequenceFlow = nodeData.find(task => task.key === flow.id);
            if (sequenceFlow && sequenceFlow.completed) {
              canvas.addMarker(flow.id, 'highlight');
            }
          } else {
            // 其他类型的流程线处理
            canvas.addMarker(flow.id, targetTask.completed ? 'highlight' : 'highlight-todo');
            canvas.addMarker(flow.targetRef.id, targetTask.completed ? 'highlight' : 'highlight-todo');
          }
        }
      })
    }
  });
}

// 用户任务节点或网关节点处理
function handleTaskOrGateway(node, canvas, nodeData, completeTask, todoTask) {
  if (completeTask) {
    const highlightType = completeTask.completed ? 'highlight' : 'highlight-todo';
    canvas.addMarker(node.id, highlightType);

    // 遍历当前节点的出线
    node.outgoing?.forEach(flow => {
      const targetTask = nodeData.find(task => task.key === flow.targetRef.id);

      if (targetTask) {
        if (todoTask && completeTask.key === todoTask.key && !todoTask.completed) {
          // 待办任务标记为高亮或待办
          canvas.addMarker(flow.id, todoTask.completed ? 'highlight' : 'highlight-todo');
          canvas.addMarker(flow.targetRef.id, todoTask.completed ? 'highlight' : 'highlight-todo');
        } else {
          if (flow.$type === 'bpmn:SequenceFlow') {
            // 顺序流程线处理
            const sequenceFlow = nodeData.find(task => task.key === flow.id);
            if (sequenceFlow && sequenceFlow.completed) {
              canvas.addMarker(flow.id, 'highlight');
            }
          } else {
            // 其他类型的流程线处理
            canvas.addMarker(flow.id, targetTask.completed ? 'highlight' : 'highlight-todo');
            canvas.addMarker(flow.targetRef.id, targetTask.completed ? 'highlight' : 'highlight-todo');
          }
        }
      }
    });
  }
}

// 开始事件节点处理
function handleStartEvent(node, canvas, nodeData) {
  // 遍历当前节点的出线
  node.outgoing.forEach(flow => {
    const completeTask = nodeData.find(task => task.key === flow.targetRef.id);
    if (completeTask) {
      // 标记当前节点和流程线为高亮
      canvas.addMarker(node.id, 'highlight-event');
      canvas.addMarker(flow.id, 'highlight');
    }
  });
}

// 结束事件节点处理
function handleEndEvent(node, canvas, nodeData) {
  // 如果最后一个任务与当前节点相同且已完成，则将当前节点标记为高亮
  const endTask = nodeData.find(task => task.key === node.id);
  if (endTask && endTask.completed) {
    canvas.addMarker(node.id, 'highlight');
  }
}

// 事件节点处理
function handleEventNode(node, canvas, nodeData, completeTask) {
  const marker = completeTask ? (completeTask.completed ? 'highlight-event' : 'highlight-event-todo') : '';
  canvas.addMarker(node.id, marker);

  // 遍历当前节点的出线
  node.outgoing?.forEach(flow => {
    if (flow.$type === 'bpmn:SequenceFlow') {
      // 顺序流程线处理
      const sequenceFlow = nodeData.find(task => task.key === flow.id);
      if (sequenceFlow && sequenceFlow.completed) {
        canvas.addMarker(flow.id, 'highlight');
      }
    }
  });
}

// 子任务处理
function handleSubProcess(subProcess, canvas, nodeData) {
  subProcess.flowElements.forEach(node => {
    const completeTask = nodeData.find(task => task.key === node.id);
    const todoTask = nodeData.find(task => !task.completed);

    // 判断节点类型
    const isTaskOrGateway = node.$type.indexOf('Task') !== -1 || node.$type.indexOf('Gateway') !== -1;
    const isStartEvent = node.$type === 'bpmn:StartEvent';
    const isEndEvent = node.$type === 'bpmn:EndEvent';
    const isEventNode = node.$type === 'bpmn:IntermediateCatchEvent' || node.$type === 'bpmn:IntermediateThrowEvent' || node.$type === 'bpmn:BoundaryEvent';

    if (isTaskOrGateway) {
      // 用户任务节点或网关节点处理
      handleTaskOrGateway(node, canvas, nodeData, completeTask, todoTask);
    } else if (isStartEvent) {
      // 开始事件节点处理
      handleStartEvent(node, canvas, nodeData);
    } else if (isEndEvent) {
      // 结束事件节点处理
      handleEndEvent(node, canvas, nodeData);
    } else if (isEventNode) {
      // 事件节点处理
      handleEventNode(node, canvas, nodeData, completeTask);
    }
  });
}

</script>

<style lang="scss">
@import "bpmn-js/dist/assets/bpmn-js.css";
@import "bpmn-js/dist/assets/diagram-js.css";
@import "@/assets/styles/flow-viewer.scss";
</style>
