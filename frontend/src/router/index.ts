import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
        { path: 'portal/rfx', name: 'portal-rfx', component: () => import('@/views/portal/PortalRfxListView.vue') },
        { path: 'portal/rfx/:id', name: 'portal-rfx-detail', component: () => import('@/views/portal/PortalRfxDetailView.vue') },
        { path: 'portal/au', name: 'portal-au', component: () => import('@/views/portal/PortalAuctionListView.vue') },
        { path: 'portal/au/:id', name: 'portal-au-detail', component: () => import('@/views/portal/PortalAuctionDetailView.vue') },
        { path: 'approval/inbox', name: 'approval-inbox', component: () => import('@/views/approval/ApprovalInboxView.vue') },
        { path: 'approval/draft', name: 'approval-draft', component: () => import('@/views/approval/ApprovalDraftView.vue') },
        { path: 'approval/delegate', name: 'approval-delegate', component: () => import('@/views/approval/DelegateView.vue') },
        { path: 'approval/template', name: 'approval-template', component: () => import('@/views/approval/LineTplView.vue') },
        { path: 'sys/code', name: 'sys-code', component: () => import('@/views/sys/CodeView.vue') },
        { path: 'sys/menu', name: 'sys-menu', component: () => import('@/views/sys/MenuView.vue') },
        { path: 'sys/role', name: 'sys-role', component: () => import('@/views/sys/RoleView.vue') },
        { path: 'sys/user', name: 'sys-user', component: () => import('@/views/sys/UserView.vue') },
        { path: 'sys/notice', name: 'sys-notice', component: () => import('@/views/sys/NoticeView.vue') },
        { path: 'pro/pr', name: 'pro-pr', component: () => import('@/views/pro/PrListView.vue') },
        { path: 'pro/pr/:id', name: 'pro-pr-detail', component: () => import('@/views/pro/PrFormView.vue') },
        { path: 'pro/rx', name: 'pro-rx', component: () => import('@/views/pro/RxListView.vue') },
        { path: 'pro/rx/:id', name: 'pro-rx-detail', component: () => import('@/views/pro/RxFormView.vue') },
        { path: 'pro/au', name: 'pro-au', component: () => import('@/views/pro/AuListView.vue') },
        { path: 'pro/au/:id', name: 'pro-au-detail', component: () => import('@/views/pro/AuFormView.vue') },
        { path: 'pro/po', name: 'pro-po', component: () => import('@/views/pro/PoListView.vue') },
        { path: 'pro/po/:id', name: 'pro-po-detail', component: () => import('@/views/pro/PoFormView.vue') },
        { path: 'pro/po/:id/print', name: 'pro-po-print', component: () => import('@/views/pro/PoPrintView.vue') },
        { path: 'pro/gr', name: 'pro-gr', component: () => import('@/views/pro/GrListView.vue') },
        { path: 'pro/gr/:id', name: 'pro-gr-detail', component: () => import('@/views/pro/GrFormView.vue') },
        { path: 'pro/pa', name: 'pro-pa', component: () => import('@/views/pro/PaListView.vue') },
        { path: 'pro/pa/:id', name: 'pro-pa-detail', component: () => import('@/views/pro/PaFormView.vue') },
        { path: 'pro/av', name: 'pro-av', component: () => import('@/views/pro/AvListView.vue') },
        { path: 'pro/av/:id', name: 'pro-av-detail', component: () => import('@/views/pro/AvFormView.vue') },
        { path: 'pro/cl', name: 'pro-cl', component: () => import('@/views/pro/ClListView.vue') },
        { path: 'pro/cl/:id', name: 'pro-cl-detail', component: () => import('@/views/pro/ClFormView.vue') },
        { path: 'pro/bc', name: 'pro-bc', component: () => import('@/views/pro/BcListView.vue') },
        { path: 'pro/bc/:id', name: 'pro-bc-detail', component: () => import('@/views/pro/BcFormView.vue') },
        { path: 'pro/ct', name: 'pro-ct', component: () => import('@/views/pro/CtListView.vue') },
        { path: 'pro/ct/:id', name: 'pro-ct-detail', component: () => import('@/views/pro/CtFormView.vue') },
        { path: 'pro/ip', name: 'pro-ip', component: () => import('@/views/pro/IpView.vue') },
        { path: 'pro/ip/rsv/:id', name: 'pro-ip-rsv-detail', component: () => import('@/views/pro/IpRsvFormView.vue') },
        { path: 'pro/cladj', name: 'pro-cladj', component: () => import('@/views/pro/CladjView.vue') },
        { path: 'base/vendor', name: 'base-vendor', component: () => import('@/views/base/VendorView.vue') },
        { path: 'base/vendor-reg', name: 'base-vendor-reg', component: () => import('@/views/base/VendorRegView.vue') },
        { path: 'base/cate-attr', name: 'base-cate-attr', component: () => import('@/views/base/CateAttrView.vue') },
        { path: 'base/item-req', name: 'base-item-req', component: () => import('@/views/base/ItemReqListView.vue') },
        { path: 'base/item-req/:id', name: 'base-item-req-detail', component: () => import('@/views/base/ItemReqFormView.vue') },
        { path: 'base/category', name: 'base-category', component: () => import('@/views/base/CategoryView.vue') },
        { path: 'base/item', name: 'base-item', component: () => import('@/views/base/ItemView.vue') },
        { path: 'srm/segment', name: 'srm-segment', component: () => import('@/views/srm/SegmentView.vue') },
        { path: 'srm/claim', name: 'srm-claim', component: () => import('@/views/srm/ClaimListView.vue') },
        { path: 'srm/claim/:id', name: 'srm-claim-detail', component: () => import('@/views/srm/ClaimFormView.vue') },
        { path: 'srm/sheet', name: 'srm-sheet', component: () => import('@/views/srm/SheetView.vue') },
        { path: 'srm/eval', name: 'srm-eval', component: () => import('@/views/srm/EvalListView.vue') },
        { path: 'srm/eval/:id', name: 'srm-eval-detail', component: () => import('@/views/srm/EvalFormView.vue') },
        { path: 'srm/audit', name: 'srm-audit', component: () => import('@/views/srm/AuditListView.vue') },
        { path: 'srm/audit/:id', name: 'srm-audit-detail', component: () => import('@/views/srm/AuditFormView.vue') },
        { path: 'srm/regal', name: 'srm-regal', component: () => import('@/views/srm/RegalView.vue') },
        { path: 'qlt/inspection', name: 'qlt-inspection', component: () => import('@/views/qlt/InspectionListView.vue') },
        { path: 'qlt/inspection/:id', name: 'qlt-inspection-detail', component: () => import('@/views/qlt/InspectionFormView.vue') },
        { path: 'report', name: 'report', component: () => import('@/views/report/ReportView.vue') },
        { path: 'ifc/monitor', name: 'ifc-monitor', component: () => import('@/views/ifc/IfMonitorView.vue') },
        { path: 'ifc/tax', name: 'ifc-tax', component: () => import('@/views/ifc/TaxbillView.vue') },
        { path: 'ifc/slip', name: 'ifc-slip', component: () => import('@/views/ifc/SlipView.vue') },
        { path: 'pro/payment', name: 'pro-payment', component: () => import('@/views/pro/PaymentView.vue') },
        { path: 'pro/mrp', name: 'pro-mrp', component: () => import('@/views/pro/MrpView.vue') },
        { path: 'base/budget', name: 'base-budget', component: () => import('@/views/base/BudgetView.vue') },
        { path: 'inv/stock', name: 'inv-stock', component: () => import('@/views/inv/StockListView.vue') },
        { path: 'inv/ledger', name: 'inv-ledger', component: () => import('@/views/inv/LedgerView.vue') },
        { path: 'inv/issue', name: 'inv-issue', component: () => import('@/views/inv/IssueListView.vue') },
        { path: 'inv/issue/:id', name: 'inv-issue-detail', component: () => import('@/views/inv/IssueFormView.vue') },
        { path: 'inv/count', name: 'inv-count', component: () => import('@/views/inv/CountListView.vue') },
        { path: 'inv/count/:id', name: 'inv-count-detail', component: () => import('@/views/inv/CountFormView.vue') },
        { path: 'pro/pp', name: 'pro-pp', component: () => import('@/views/pro/PpListView.vue') },
        { path: 'pro/pp/:id', name: 'pro-pp-detail', component: () => import('@/views/pro/PpFormView.vue') },
        { path: 'qlt/nc', name: 'qlt-nc', component: () => import('@/views/qlt/NcListView.vue') },
        { path: 'qlt/nc/:id', name: 'qlt-nc-detail', component: () => import('@/views/qlt/NcFormView.vue') },
      ],
    },
  ],
})

// 인증 가드
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isLoggedIn) {
    return { name: 'home' }
  }
})

export default router
