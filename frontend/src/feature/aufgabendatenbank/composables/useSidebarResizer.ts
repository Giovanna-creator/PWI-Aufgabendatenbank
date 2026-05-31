import { ref, onUnmounted } from 'vue'

export function useSidebarResizer(minWidth = 150, maxOffset = 200) {
  const sidebarWidth = ref(300)
  const isResizing = ref(false)
  const containerRef = ref<HTMLElement | null>(null)

  const startResizing = () => {
    isResizing.value = true
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', stopResizing)
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
  }

  const handleMouseMove = (event: MouseEvent) => {
    if (!isResizing.value || !containerRef.value) return
    const containerRect = containerRef.value.getBoundingClientRect()
    const newWidth = event.clientX - containerRect.left
    if (newWidth > minWidth && newWidth < containerRect.width - maxOffset) {
      sidebarWidth.value = newWidth
    }
  }

  const stopResizing = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', stopResizing)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  onUnmounted(() => stopResizing())

  return { sidebarWidth, isResizing, containerRef, startResizing }
}
