# WebStormPlugins
Simple inspections to highlight unknown CCS classes used in 'classname' and to highlight console.logs in production code

1) Search for:

```
       <Components className={styles.XYZ}>
        ...
       </Components>
```
 where XYZ is not found in imported **styles**

2) Search for:
  
```
      console.log('XYZ') in PRODUCTION (non tests) files
```
