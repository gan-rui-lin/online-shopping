import type { ComposerTranslation } from 'vue-i18n'
import { MerchantApplyStatus, OrderStatus, PayStatus, ProductStatus } from '@/constants/enums'

export function getOrderStatusLabel(t: ComposerTranslation, status: number): string {
  const map: Record<number, string> = {
    [OrderStatus.UNPAID]: 'status.order.unpaid',
    [OrderStatus.TO_SHIP]: 'status.order.toShip',
    [OrderStatus.TO_RECEIVE]: 'status.order.toReceive',
    [OrderStatus.COMPLETED]: 'status.order.completed',
    [OrderStatus.CANCELLED]: 'status.order.cancelled',
    [OrderStatus.REFUNDING]: 'status.order.refunding',
    [OrderStatus.REFUNDED]: 'status.order.refunded',
  }
  return t(map[status] || 'common.unknown')
}

export function getPayStatusLabel(t: ComposerTranslation, status: number): string {
  const map: Record<number, string> = {
    [PayStatus.UNPAID]: 'status.pay.unpaid',
    [PayStatus.PAID]: 'status.pay.paid',
    [PayStatus.REFUNDED]: 'status.pay.refunded',
  }
  return t(map[status] || 'common.unknown')
}

export function getProductStatusLabel(t: ComposerTranslation, status: number): string {
  const map: Record<number, string> = {
    [ProductStatus.DRAFT]: 'status.product.draft',
    [ProductStatus.ON_SHELF]: 'status.product.onShelf',
    [ProductStatus.OFF_SHELF]: 'status.product.offShelf',
  }
  return t(map[status] || 'common.unknown')
}

export function getProductAuditStatusLabel(t: ComposerTranslation, status: number): string {
  const map: Record<number, string> = {
    0: 'status.productAudit.pending',
    1: 'status.productAudit.approved',
    2: 'status.productAudit.rejected',
  }
  return t(map[status] || 'common.unknown')
}

export function getMerchantApplyStatusLabel(t: ComposerTranslation, status: number): string {
  const map: Record<number, string> = {
    [MerchantApplyStatus.PENDING]: 'status.merchantApply.pending',
    [MerchantApplyStatus.APPROVED]: 'status.merchantApply.approved',
    [MerchantApplyStatus.REJECTED]: 'status.merchantApply.rejected',
  }
  return t(map[status] || 'common.unknown')
}
