# ADR-004: 住所情報を AddressFormData として UserFormData から切り出す

Date: 2026-05-29
Status: Accepted

## Context

`dads:addressField` に渡すモデルをどう設計するかを検討した。

当初は `UserFormData`（FlowScoped）をそのまま渡す案を検討した。
しかし `UserFormData` には名前・メール・電話番号など住所以外のフィールドも含まれるため、
`dads:addressField` が住所フォームのみを必要とする別画面で使いにくくなるという問題があった。
コンポーネントが特定の Bean 名に依存することも再利用性を損なう。

## Decision

**住所関連のフィールドと BV アノテーションを `AddressFormData`（POJO）として切り出す。**

- `AddressFormData` は CDI スコープを持たない純粋な POJO
- `UserFormData` は `AddressFormData` をフィールドとして内包し、`@Valid` で BV を連鎖させる
- `dads:addressField` は `AddressFormData` を受け取る（`value="#{userFormData.address}"`）

```
UserFormData (@FlowScoped)
  ├── name, email, phoneNumber（＋BV）
  └── @Valid AddressFormData address
        ├── postalCode（＋BV）
        ├── prefecture, city, streetAddress（＋BV）
        └── buildingName（任意）
```

## Consequences

**良い点**
- `dads:addressField` が `UserFormData` に依存しないため、住所フォームが必要な任意の画面で再利用できる
- 住所に関する BV・フィールド定義が `AddressFormData` に凝集され、変更箇所が明確になる
- `@Valid` による BV 連鎖でバリデーションの一元管理は維持される

**トレードオフ**
- クラス数が1つ増える
- `UserFormData` から住所フィールドに直接アクセスする場合は `userFormData.address.postalCode` のように2段階の参照になる
