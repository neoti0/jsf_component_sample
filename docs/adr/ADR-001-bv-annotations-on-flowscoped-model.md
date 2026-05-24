# ADR-001: Bean Validation アノテーションを FlowScoped モデルクラスに集約する

Date: 2026-05-29
Status: Accepted

## Context

ユーザー登録フローのバリデーション定義をどのクラスに置くかを検討した。

候補として以下の3案があった。

**案A: Backing Bean（@ViewScoped）にBVを定義**
- フォームのバインド先が Backing Bean のフィールドなので、JSF-BV 連携が自然に動く
- ただし FlowScoped モデルクラスにも同じフィールドを持つ場合、BV を二重に定義することになる

**案B: FlowScoped モデルクラスにBVを定義し、Backing Bean にも複製**
- 「データの持ち主」に BV を置く設計だが、BV の定義が2か所に分散する
- 修正漏れのリスクが高い

**案C: FlowScoped モデルクラスにBVを定義し、フォームから直接バインド**
- BV の定義がモデルクラスに一元化される
- フォームが `#{userFormData.name}` で直接 FlowScoped を参照するため Backing Bean に BV 不要

## Decision

**案C を採用する。**

Bean Validation アノテーションは `UserFormData`（`@FlowScoped`）のフィールドに定義し、
XHTML フォームからは `#{userFormData.フィールド名}` で直接バインドする。
Backing Bean には BV アノテーションを一切書かない。

## Consequences

**良い点**
- BV の定義が `UserFormData` に集約され、単一の変更点（Single Source of Truth）になる
- フィールド追加・バリデーション変更時の修正箇所が1か所で済む
- Backing Bean が薄くなり、ナビゲーションロジックに集中できる

**トレードオフ**
- フォームのバインド先が FlowScoped になるため、Flow の外（通常の RequestScoped など）では `userFormData` が参照できない点に注意が必要
- 入力中のデータは常に FlowScoped に即時反映される（「編集中の下書き」と「確定データ」を分離したい場合は別途設計が必要）
