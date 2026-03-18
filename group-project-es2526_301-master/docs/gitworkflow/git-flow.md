# GitHub Flow

## Context

This **README** serves as a guide for the developers to work using the same workflow.

## Team Policies for using Git/Github

- Only work/add features/fix bugs in the **dev** branch;
- The code only migrates to the **main** branch to deploy releases and it's **DevOps's** responsability;
- Every time that there is a new feature to add or a bugfix to do, a **new** branch from the **dev** must be created;
- When there are more than one person working in a feature/bugfix, a branch may be also created for every major adition;
- Only confirm a pull request after, at least, another contributor checked it;
- Release pull requests are validated by the **Product Owner**.

_Every rule can have some exceptions in specific cases_

## Working Branch

Go to the **dev**elopment branch.

```bash
git checkout dev
git pull
```

## Issues

#### If there is no issue created for the work that you are going to do:

- create the issue and assign to yourself
- choose the repository's project in the **project** section
- leave page
- enter in the issue again and choose the current iteration in the **project** section

## Create a new branch

You are creating a branch either to work on a **feature** or a **bugfix**, for that you need to make a new branch with the following templates:

- `feature/<FEATURE_NAME>` - to add a new feature
- `bugfix/<WHAT_YOU_ARE_FIXING>` - to fix a bug
- `hotfix/<WHAT_YOU_ARE_FIXING>` - to fix an emergency bug in the **main** branch
- `refact/<FEATURE_YOU_ARE_REFACTORING>` - to continue/refactor a feature
- `docs/<DOCUMENTATION_NAME>` - to add/edit something about documentation

Both names need to be easy to understand of what is adding or what is fixing. Eg. **feature/menu**, **bugfix/menuLoading**.

#### Always check if you are in the **dev** branch!:

```bash
git branch
```

#### Finally, create the new branch:

```bash
git checkout -b feature/<placeholder>
```

_If you want to make a branch from a feature/bugfix, go to the desired branch and name it with the same beginning_

_Eg. feature/menu -> feature/menu/new_animation_

## Add, commit & push

Try to add and commit every time you make big changes in important files

```bash
git add .
git commit -m "<message>"
```

#### The commits messages of a branch should follow the template

- `type: general idea of what the commit is doing`

#### Different types:

- `feat`
- `fix`
- `hfix`
- `refact`
- `docs`

#### Pushing for the first time

```bash
git push --set-upstream origin <branch_name>
```

#### Pushing

```bash
git push
```

## Final Push

#### Rebasing

Before doing the **final** push, you need to **rebase** with the **dev** branch so the **pull request** is possible

First, go to the **dev** and **pull** to have the last version of the dev:

```bash
git checkout dev
git pull
```

Then, go back to your branch and rebase

```bash
git checkout you_branch
git rebase dev
```

#### Conflicts

If there are conflicts, you need to go to your editor and resolve them by selecting what do you want inside the **>>>HEAD >>>branch** tags.

#### Every time you resolve a conflict do:

```bash
git add .
git commit -m "rebasing dev"
git rebase --continue
```

#### Now you can push

```bash
git push
```

#### If occurs an error pushing and you did the rebasing correctly do:

##### Please double-check that the result of the rebase is the desired one

```bash
git push --force
```

## Pull Request

- Go to the repository and click in **Compare & pull request**;

### - Choose the **dev** branch in the **base!**;

- For the template, you should add to the **url** the query parameter **&template=template_name.md**

For example: *https://github.com/detiuaveiro/group-project-es2526_301/compare/feature/new_feature?expand=1&template=feature_template.md*

Check **docs/PULL_REQUEST_TEMPLATE** for the templates that you can use

Possible templates:

- `bugfix_template.md`
- `docs_template.md`
- `feature_template.md`
- `hotfix_template.md`
- `refactor_template.md`

Now:

- Choose a team member as a reviewer;
- Click in **Create pull request**;
- Click in **Merge pull request** if the review was validated and if the reviewer didn't merged himself.

Finally you can locally remove the branch do:

```bash
git checkout dev
git pull
git branch -D <branch_to_delete>
```

## Code Reviews & Testing

- Every Pull Request must be reviewed by at least **one other team member** before merging.
- Authors cannot approve or merge their own PRs.
- Reviews should verify:
  - Code readability and maintainability.
  - Consistency with coding style and architecture.
  - Adequate tests are included (unit/integration) for new or modified code.
  - No sensitive data (API keys, passwords) is present in commits.
- Pull Requests without relevant tests will not be approved.
- Test coverage must not decrease compared to the current baseline.
- The priority is **meaningful tests** that validate real behavior and edge cases, not just coverage numbers.
- Reviewers should give constructive feedback, and authors should resolve comments before merge.

### Common Review Messages

- **LGTM** (Looks good to me)
- **Approved — ready to merge**
- **Nice job, clear and clean code**
- **Please add a test for this case before merging**
- **This part could be simplified — consider refactoring**
