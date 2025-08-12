#!/bin/bash

# RAG基准集评估脚本
# 用于CI/CD质量闸门，验证RAG系统质量是否满足阈值要求

set -e

# 配置参数
EVAL_SERVICE_URL="${EVAL_SERVICE_URL:-http://localhost:8088}"
BASELINE_FILE="${BASELINE_FILE:-scripts/baseline-dataset.json}"
OUTPUT_FILE="${OUTPUT_FILE:-target/baseline-eval-result.json}"
CONFIG_FILE="${CONFIG_FILE:-scripts/eval-thresholds.yaml}"

# 默认阈值（与配置文件保持一致）
FAITHFULNESS_MIN=0.85
ANSWER_RELEVANCY_MIN=0.80
CONTEXT_PRECISION_MIN=0.70
CONTEXT_RECALL_MIN=0.75

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== RAG基准集评估开始 ===${NC}"
echo "评估服务地址: $EVAL_SERVICE_URL"
echo "基准集文件: $BASELINE_FILE"
echo "输出文件: $OUTPUT_FILE"

# 检查基准集文件是否存在
if [ ! -f "$BASELINE_FILE" ]; then
    echo -e "${RED}错误: 基准集文件不存在: $BASELINE_FILE${NC}"
    exit 1
fi

# 检查评估服务健康状态
echo -e "${BLUE}检查评估服务健康状态...${NC}"
health_response=$(curl -s -f "$EVAL_SERVICE_URL/health" || echo "failed")
if [ "$health_response" = "failed" ]; then
    echo -e "${RED}错误: 评估服务不可用: $EVAL_SERVICE_URL${NC}"
    exit 1
fi

service_status=$(echo "$health_response" | jq -r '.status // "unknown"')
service_version=$(echo "$health_response" | jq -r '.version // "unknown"')
echo -e "${GREEN}评估服务状态: $service_status, 版本: $service_version${NC}"

# 加载阈值配置（如果存在）
if [ -f "$CONFIG_FILE" ]; then
    echo -e "${BLUE}加载阈值配置: $CONFIG_FILE${NC}"
    # 使用yq或其他工具解析YAML（这里简化处理）
    # FAITHFULNESS_MIN=$(yq eval '.thresholds.faithfulness' "$CONFIG_FILE")
    # 等等...
fi

echo -e "${BLUE}使用阈值配置:${NC}"
echo "  忠实度 ≥ $FAITHFULNESS_MIN"
echo "  答案相关性 ≥ $ANSWER_RELEVANCY_MIN"
echo "  上下文精确度 ≥ $CONTEXT_PRECISION_MIN"
echo "  上下文召回度 ≥ $CONTEXT_RECALL_MIN"

# 创建输出目录
mkdir -p "$(dirname "$OUTPUT_FILE")"

# 执行评估
echo -e "${BLUE}执行基准集评估...${NC}"
eval_start_time=$(date +%s)

# 发送评估请求
eval_response=$(curl -s -f -X POST "$EVAL_SERVICE_URL/eval" \
    -H "Content-Type: application/json" \
    -d @"$BASELINE_FILE" || echo "failed")

eval_end_time=$(date +%s)
eval_duration=$((eval_end_time - eval_start_time))

if [ "$eval_response" = "failed" ]; then
    echo -e "${RED}错误: 评估请求失败${NC}"
    exit 1
fi

# 保存评估结果
echo "$eval_response" > "$OUTPUT_FILE"
echo -e "${GREEN}评估结果已保存: $OUTPUT_FILE${NC}"

# 解析评估结果
total_items=$(echo "$eval_response" | jq -r '.aggregate.total_items // 0')
pass_threshold=$(echo "$eval_response" | jq -r '.aggregate.pass_threshold // false')

avg_faithfulness=$(echo "$eval_response" | jq -r '.aggregate.avg_faithfulness // 0')
avg_answer_relevancy=$(echo "$eval_response" | jq -r '.aggregate.avg_answer_relevancy // 0')
avg_context_precision=$(echo "$eval_response" | jq -r '.aggregate.avg_context_precision // 0')
avg_context_recall=$(echo "$eval_response" | jq -r '.aggregate.avg_context_recall // 0')

failed_metrics=$(echo "$eval_response" | jq -r '.aggregate.failed_metrics[]?' || echo "")

echo -e "${BLUE}=== 评估结果摘要 ===${NC}"
echo "评估时间: ${eval_duration}秒"
echo "评估项数: $total_items"
echo "整体通过: $pass_threshold"
echo ""
echo "指标结果:"
echo "  忠实度: $(printf "%.3f" "$avg_faithfulness") (阈值: $FAITHFULNESS_MIN)"
echo "  答案相关性: $(printf "%.3f" "$avg_answer_relevancy") (阈值: $ANSWER_RELEVANCY_MIN)"
echo "  上下文精确度: $(printf "%.3f" "$avg_context_precision") (阈值: $CONTEXT_PRECISION_MIN)"
echo "  上下文召回度: $(printf "%.3f" "$avg_context_recall") (阈值: $CONTEXT_RECALL_MIN)"

# 检查各项指标
failed_count=0
failed_details=""

# 检查忠实度
if (( $(echo "$avg_faithfulness < $FAITHFULNESS_MIN" | bc -l) )); then
    failed_count=$((failed_count + 1))
    failed_details="$failed_details\n  • 忠实度不达标: $(printf "%.3f" "$avg_faithfulness") < $FAITHFULNESS_MIN"
fi

# 检查答案相关性
if (( $(echo "$avg_answer_relevancy < $ANSWER_RELEVANCY_MIN" | bc -l) )); then
    failed_count=$((failed_count + 1))
    failed_details="$failed_details\n  • 答案相关性不达标: $(printf "%.3f" "$avg_answer_relevancy") < $ANSWER_RELEVANCY_MIN"
fi

# 检查上下文精确度
if (( $(echo "$avg_context_precision < $CONTEXT_PRECISION_MIN" | bc -l) )); then
    failed_count=$((failed_count + 1))
    failed_details="$failed_details\n  • 上下文精确度不达标: $(printf "%.3f" "$avg_context_precision") < $CONTEXT_PRECISION_MIN"
fi

# 检查上下文召回度
if (( $(echo "$avg_context_recall < $CONTEXT_RECALL_MIN" | bc -l) )); then
    failed_count=$((failed_count + 1))
    failed_details="$failed_details\n  • 上下文召回度不达标: $(printf "%.3f" "$avg_context_recall") < $CONTEXT_RECALL_MIN"
fi

echo ""
if [ "$failed_count" -eq 0 ] && [ "$pass_threshold" = "true" ]; then
    echo -e "${GREEN}✅ 质量闸门通过: 所有指标均达到阈值要求${NC}"
    echo -e "${GREEN}=== 基准集评估成功 ===${NC}"
    exit 0
else
    echo -e "${RED}❌ 质量闸门失败: $failed_count 项指标不达标${NC}"
    if [ -n "$failed_details" ]; then
        echo -e "${RED}失败详情:${NC}"
        echo -e "$failed_details"
    fi
    
    # 输出低分样本的trace_id供调试
    echo ""
    echo -e "${YELLOW}建议操作:${NC}"
    echo "1. 检查评估结果文件: $OUTPUT_FILE"
    echo "2. 分析不达标指标的原因"
    echo "3. 优化RAG系统配置或算法"
    echo "4. 更新基准集或调整阈值（谨慎操作）"
    
    echo -e "${RED}=== 基准集评估失败 ===${NC}"
    exit 1
fi