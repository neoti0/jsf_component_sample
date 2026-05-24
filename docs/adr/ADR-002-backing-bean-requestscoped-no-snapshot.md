# ADR-002: Backing Bean を @RequestScoped とし、ViewScoped スナップショット方式を採用しない

Date: 2026-05-29
Status: Accepted

## Context

ユーザー登録3画面（入力・確認・完了）のコントローラークラスのスコープと、
画面間のデータ受け渡し方式を検討した。

**検討した方式: ViewScoped スナップショット方式**
- Backing Bean を `@ViewScoped` にして、画面内の入力値を保持する
- 「確認する」ボタン押下時（バリデーション通過後）に Backing Bean のフィールド値を FlowScoped モデルに手動コピーする
- FlowScoped モデルはバリデーション済みの断面データのみを保持する「スナップショット」として機能する

この方式の設計意図：
- 「編集中の状態（Backing Bean）」と「確定済みの状態（FlowScoped）」を明確に分離できる
- FlowScoped が常にクリーンな確定データのみを持つ

## Decision

**ViewScoped スナップショット方式を採用せず、Backing Bean は `@RequestScoped` とする。**

ADR-001 で BV を FlowScoped モデルに集約し、フォームを直接 FlowScoped にバインドする設計を採用した結果、
Backing Bean がフォームデータを保持する必要がなくなった。

- Backing Bean は `@RequestScoped` とし、ナビゲーションメソッドと `@Inject` のみを持つ薄い実装にする
- フォームデータは FlowScoped モデル（`UserFormData`）が直接保持する
- 画面間の「戻る」ナビゲーション時も FlowScoped にデータが残るため、再投入ロジックが不要

## Consequences

**良い点**
- Backing Bean → FlowScoped へのコピーロジックが不要になり、実装がシンプルになる
- 「戻る」ボタンで入力画面に戻った際、`@PostConstruct` での FlowScoped → Backing Bean への再投入が不要
- Backing Bean が薄くなり、テストしやすい

**トレードオフ**
- FlowScoped モデルが「入力中の未確定データ」も保持し続けるため、「確定済みスナップショット」という明確な状態分離はない
- 実用上はバリデーションが通らないと画面遷移しないため、確認画面到達時点では FlowScoped に有効なデータが入っており問題は生じない
