<template>
  <div class="app-container">
    <el-timeline>
      <el-timeline-item
          v-for="(item,index ) in recordList"
          :key="index"
          :color="setColor(item.finishTime)"
      >
        <p style="font-weight: 700">{{ item.taskName }}</p>
        <el-card :body-style="{ padding: '10px' }">
          <!--todo: descriptions-item 样式不一致,怎么调整优化一下-->
          <el-descriptions :column="1" size="small" border :key="index">
            <template v-if="item.refill">
              <el-descriptions-item v-if="item.startUserName">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <User/>
                    </el-icon>
                    申请人
                  </div>
                </template>
                {{ item.startUserName }}
                <el-tag effect="plain" type="info" size="small">{{ item.startDeptName }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <Timer/>
                    </el-icon>
                    申请时间
                  </div>
                </template>
                {{ item.createTime }}
              </el-descriptions-item>
              <el-descriptions-item v-if="item.commentList.length > 0">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <ChatDotSquare/>
                    </el-icon>
                    申请备注
                  </div>
                </template>
                <span :key="index" v-for="(comment, index) in item.commentList">{{ comment.content }}</span>
              </el-descriptions-item>
            </template>
            <template v-else-if="item.revoke">
              <el-descriptions-item>
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <Timer/>
                    </el-icon>
                    撤回时间
                  </div>
                </template>
                {{ item.createTime }}
              </el-descriptions-item>
              <el-descriptions-item v-if="item.commentList.length > 0">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <ChatDotSquare/>
                    </el-icon>
                    撤回详情
                  </div>
                </template>
                <span :key="index" v-for="(comment, index) in item.commentList">{{ comment.content }}</span>
              </el-descriptions-item>
            </template>
            <template v-else>
              <el-descriptions-item v-if="item.assigneeName">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <User/>
                    </el-icon>
                    办理人
                  </div>
                </template>
                {{ item.assigneeName }}
                <el-tag effect="plain" type="info" size="small">{{ item.deptName }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-if="item.candidate">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <User/>
                    </el-icon>
                    候选办理
                  </div>
                </template>
                {{ item.candidate }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <Timer/>
                    </el-icon>
                    办理耗时
                  </div>
                </template>
                {{ item.duration }}（ {{ item.createTime }} - {{ item.finishTime }} ）
              </el-descriptions-item>
              <el-descriptions-item v-if="item.commentList.length > 0">
                <template #label>
                  <div class="cell-item">
                    <el-icon :style="iconStyle">
                      <ChatDotSquare/>
                    </el-icon>
                    处理意见
                  </div>
                </template>
                <el-collapse :model-value="getExpandNames(item.commentList)">
                  <el-collapse-item :name="index" :key="index" v-for="(comment, index) in item.commentList">
                    <template #title>
                      <el-tag effect="plain" size="small" :type="comment.style">{{ comment.remark }}</el-tag>
                      <span style="margin-left: 5px;color: #909399;font-size: 12px">{{ comment.dateTime }}</span>
                    </template>
                    <span>{{ comment.content }}</span>
                  </el-collapse-item>
                </el-collapse>
              </el-descriptions-item>
            </template>
          </el-descriptions>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup name="FlowRecord">
const recordList = ref([]);
/** 组件传值  */
const props = defineProps({
  flowRecordList: {
    type: Array,
    default: () => []
  }
});

recordList.value = props.flowRecordList;
const iconStyle = computed(() => {
  const marginMap = {
    large: '8px',
    default: '6px',
    small: '4px',
  }
  return {
    marginRight: marginMap.small,
  }
})

/** 设置color */
function setColor(val) {
  if (val) {
    return "#2bc418";
  } else {
    return "#b3bdbb";
  }
}

// 展开所有的意见
function getExpandNames(item){
  return item.map((_, index) => index);
}
</script>

<style scoped>

.cell-item {
  display: flex;
  align-items: center;
  min-width: 60px;
  white-space: nowrap;
}
</style>
