# TODO.md — タスク管理

## 完了済み

- [x] プロジェクト初期構築（Jakarta EE 10 / Faces 4.0 / WildFly 31 / Docker）
- [x] ユーザー登録フォーム（index.xhtml）
- [x] ユーザー一覧（list.xhtml）
- [x] DADS 複合コンポーネント実装（`dads:inputField`、`dads:button`）
- [x] 郵便番号検索（Ajax、ダミー実装）
- [x] 複合コンポーネントの Ajax render 不具合修正（ルート要素に `cc.clientId` を付与）
- [x] バリデーションを Jakarta Bean Validation に一本化（`@NotBlank`、`@Pattern`）
- [x] 空文字列の null 変換設定（`INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL`）
- [x] JSF `required` 属性をすべて撤廃し Bean Validation に統一

---

## 未着手

### フェーズ1: クラス設計の整理

- [x] `model/AddressFormData.java` 作成（郵便番号・都道府県・市区町村・町村番地・ビル名 + BV アノテーション）
- [x] `model/UserFormData.java` 作成（名前・メール・電話番号 + `@Valid AddressFormData` を内包、`@Named @FlowScoped`）
- [x] `backing/RegisterInputBacking.java` 作成（`@Named @RequestScoped`、入力画面と 1:1）
- [x] `backing/RegisterConfirmBacking.java` 作成（`@Named @RequestScoped`、確認画面と 1:1）
- [x] `backing/RegisterCompleteBacking.java` 作成（`@Named @RequestScoped`、完了画面と 1:1）
- [x] `list/UserListBean.java` 作成（`@Named @SessionScoped`、登録済みユーザーのリスト保持）
- [x] 現状の `UserBean.java` を上記クラスへ移行・削除

### フェーズ2: Faces Flow 導入

- [x] Flow 定義を追加（Flow ID: `register`、`faces-config.xml` に `<flow-definition>` で定義）
- [x] Flow ノード設計（`register-input` → `register-confirm` → `register-complete` → `list`）

### フェーズ3: 画面追加

- [x] `register-input.xhtml` 作成（`index.xhtml` を Flow 対応に移行）
- [x] `register-confirm.xhtml` 作成（入力確認画面）
  - [x] 「登録する」ボタン → `UserListBean` への格納 → 完了画面遷移
  - [x] 「戻る」ボタン → 入力画面へ戻る（`AddressFormData` / `UserFormData` の値は保持）
- [x] `register-complete.xhtml` 作成（完了画面）
  - [x] 「一覧を見る」リンク → `list.xhtml`（Flow 終了）
- [x] `list.xhtml` を `UserListBacking` 経由の `dads:table` に移行

### フェーズ4: コンポーネント追加

#### dads:addressField（カスタムコンポーネント）
- [x] `AddressCandidate.java` 作成（候補住所を表す POJO）
- [x] `AddressSearchService.java` 作成（`@Named @RequestScoped`、zipcloud 実 API で `List<AddressCandidate>` を返す）
- [x] `AddressFieldComponent.java` 作成（`UIInput` 継承、`@FacesComponent` で登録、候補リストの状態保持）
- [x] `AddressFieldRenderer.java`：composite component（addressField.xhtml）がレンダリングを担当するため不要と判断
- [x] 検索結果 0件: エラーメッセージ表示
- [x] 検索結果 1件: `AddressFormData` に自動入力
- [x] 検索結果 2件以上: 候補選択モーダル表示（ラジオボタン＋確定・キャンセルボタン）

#### dads:outputField（複合コンポーネント）
- [x] `resources/dads/outputField.xhtml` 作成（確認画面用ラベル＋値の読み取り専用表示）

#### dads:illustration（複合コンポーネント）
- [x] `resources/dads/illustration.xhtml` 作成（完了画面用イラスト表示）

#### dads:table（複合コンポーネント）
- [x] `resources/dads/table.xhtml` 作成（`headers` / `rows` を受け取り DADS テーブルを描画）
- [x] `list/UserListBacking.java` 作成（`getTableHeaders()` / `getTableRows()` で表示用データを整形）

### フェーズ5: バリデーション・API 強化

- [x] 郵便番号検索 API を実 API（zipcloud）に差し替え（AddressSearchService で実装済み）
- [x] `AddressSearchService` の複数件返却ロジックを実装（zipcloud の複数結果をそのまま返却）

---

## 将来対応（優先度低）

- [ ] データ永続化（JPA + H2 または PostgreSQL）
- [ ] 一覧画面に削除機能を追加
- [ ] 電話番号の形式バリデーション
- [ ] ページネーション
