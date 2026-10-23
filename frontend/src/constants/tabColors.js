/**
 * 서버는 색 "이름"(PEACH...)만 저장하고, 실제 색은 프론트가 정한다.
 * tab: 탭 스티커 색 / tint: 탭을 골랐을 때 메모 영역 배경 / ink: 그 위 글자색 / accent: 버튼·포인트
 */
export const TAB_COLORS = {
  PEACH: { label: '복숭아', tab: '#FFD6A5', tint: '#FFF4E6', ink: '#7A4E17', accent: '#E8954A' },
  MINT: { label: '민트', tab: '#CDEAC0', tint: '#F0F8EC', ink: '#3F6230', accent: '#6FAF57' },
  SKY: { label: '하늘', tab: '#BFD7F5', tint: '#EDF4FD', ink: '#2D4E78', accent: '#5B8FD6' },
  LAVENDER: { label: '라벤더', tab: '#E3CCF2', tint: '#F7F0FC', ink: '#5A3A72', accent: '#A27AC6' },
  PINK: { label: '벚꽃', tab: '#F8C8D8', tint: '#FDF0F4', ink: '#7A3550', accent: '#DD7E9E' },
  LEMON: { label: '레몬', tab: '#F7E7A1', tint: '#FDF9E6', ink: '#6B5A12', accent: '#CCAF2E' },
}

export const COLOR_KEYS = Object.keys(TAB_COLORS)

export const MAX_TABS = 8

export function paletteOf(color) {
  return TAB_COLORS[color] ?? TAB_COLORS.PEACH
}
